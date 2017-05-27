package com.youzhixu.registry.eureka.transform;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.youzhixu.registry.eureka.transform.Request.Options;


/**
 * 默认的HttpClient仅支持gzip
 */
/*package-private*/final class BasicHttpClient {
  /**
   * The HTTP Content-Length header field name.
   */
  static final String CONTENT_LENGTH = "Content-Length";
  /**
   * The HTTP Content-Encoding header field name.
   */
  static final String CONTENT_ENCODING = "Content-Encoding";
  /**
   * The HTTP Retry-After header field name.
   */
  static final String RETRY_AFTER = "Retry-After";
  /**
   * Value for the Content-Encoding header that indicates that GZIP encoding is in use.
   */
  static final String ENCODING_GZIP = "gzip";

  private final SSLSocketFactory sslContextFactory;
  private final HostnameVerifier hostnameVerifier;

  /**
   * Null parameters imply platform defaults.
   */
  public BasicHttpClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
    this.sslContextFactory = sslContextFactory;
    this.hostnameVerifier = hostnameVerifier;
  }

  public Response execute(Request request, Options options) throws IOException {
    HttpURLConnection connection = convertAndSend(request, options);
    return convertResponse(connection);
  }

  private HttpURLConnection convertAndSend(Request request, Options options) throws IOException {
    final HttpURLConnection connection =
        (HttpURLConnection) new URL(request.url()).openConnection();
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection sslCon = (HttpsURLConnection) connection;
      if (sslContextFactory != null) {
        sslCon.setSSLSocketFactory(sslContextFactory);
      }
      if (hostnameVerifier != null) {
        sslCon.setHostnameVerifier(hostnameVerifier);
      }
    }
    connection.setConnectTimeout(options.connectTimeoutMillis());
    connection.setReadTimeout(options.readTimeoutMillis());
    connection.setAllowUserInteraction(false);
    connection.setInstanceFollowRedirects(true);
    connection.setRequestMethod(request.method());

    Collection<String> contentEncodingValues = request.headers().get(CONTENT_ENCODING);
    boolean gzipEncodedRequest =
        contentEncodingValues != null && contentEncodingValues.contains(ENCODING_GZIP);

    boolean hasAcceptHeader = false;
    Integer contentLength = null;
    for (String field : request.headers().keySet()) {
      if (field.equalsIgnoreCase("Accept")) {
        hasAcceptHeader = true;
      }
      for (String value : request.headers().get(field)) {
        if (field.equals(CONTENT_LENGTH)) {
          if (!gzipEncodedRequest) {
            contentLength = Integer.valueOf(value);
            connection.addRequestProperty(field, value);
          }
        } else {
          connection.addRequestProperty(field, value);
        }
      }
    }
    // Some servers choke on the default accept string.
    if (!hasAcceptHeader) {
      connection.addRequestProperty("Accept", "*/*");
    }

    if (request.body() != null) {
      if (contentLength != null) {
        connection.setFixedLengthStreamingMode(contentLength);
      } else {
        connection.setChunkedStreamingMode(8196);
      }
      connection.setDoOutput(true);
      OutputStream out = connection.getOutputStream();
      if (gzipEncodedRequest) {
        out = new GZIPOutputStream(out);
      }
      try {
        out.write(request.body());
      } finally {
        try {
          out.close();
        } catch (IOException suppressed) { // NOPMD
        }
      }
    }
    return connection;
  }

  private Response convertResponse(HttpURLConnection connection) throws IOException {
    int status = connection.getResponseCode();
    String reason = connection.getResponseMessage();

    if (status < 0) {
      throw new IOException(format("Invalid status(%s) executing %s %s", status,
          connection.getRequestMethod(), connection.getURL()));
    }

    Map<String, Collection<String>> headers = new LinkedHashMap<String, Collection<String>>();
    for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
      // response message
      if (field.getKey() != null) {
        headers.put(field.getKey(), field.getValue());
      }
    }

    Integer length = connection.getContentLength();
    if (length == -1) {
      length = null;
    }
    String responseEncoding = connection.getContentEncoding();
    boolean gzipedContent = "gzip".equalsIgnoreCase(responseEncoding) ? true : false;
    InputStream stream;
    if (status >= 400) {
      stream = connection.getErrorStream();
    } else {
      stream = connection.getInputStream();
    }
    // we only support gzip format compression
    if (gzipedContent) {
      //default buffer size : 512B
      stream = new GZIPInputStream(stream);
    }
    return Response.create(status, reason, headers, stream, length);
  }
}
