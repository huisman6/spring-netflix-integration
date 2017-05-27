package com.youzhixu.registry.eureka.transform;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.lianjia.sh.se.dummy.eureka.client.discovery.DiscoveryClient;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.resources.ServerCodecs;
import com.youzhixu.registry.eureka.EurekaServerApplication;
import com.youzhixu.registry.eureka.transform.Response.Body;

/**
 * 安全覆盖测试，发布版本之前，必须通过测试
 * </p>
 * 测试方案：
 * </p>
 * 1，检测ServerCodecProxy是否替换了ServerCodecs（可获取bean，判断className）
 * </p>
 * 2，junit启动两个eureka server节点： http://localhost:8761,http://localhost:8762，</br>
 * 请求以上泄漏token的接口，检查是否存在META_CLIENT_TOKEN（不可存在）；</br>
 * 可new两个SpringBootApplication，然后使用dummy-eureka-client注册几个客户端来实现</br>
 * 3，junit分别调用两个SpringBoot的DiscoveryClient,检查META_CLIENT_TOKEN是否存在（一定要有，双节点都要有）
 * </p>
 * 4，检查接口：PUT /eureka/apps/{vip}/{instanceId}/metadata是否能访问
 * </p>
 * 5，断言eureka-core里的com.netflix.eureka.resources里接口方法的签名是否变动
 * </p>
 */
public class SecurityCoverTest {
  private static final Logger logger = LoggerFactory.getLogger(SecurityCoverTest.class);
  /**
   * 节点1
   */
  static ConfigurableApplicationContext DISCOVERY_NODE1;
  /**
   * 节点2
   */
  static ConfigurableApplicationContext DISCOVERY_NODE2;

  static final String EUREKA_CLIENT1_NAME = "eureka-server-test-client1";
  static final String EUREKA_CLIENT2_NAME = "eureka-server-test-client2";
  final String CLIENT_PREFIX = "overseas-";
  static List<DiscoveryClient> EUREKA_CLIENTS;
  static BasicHttpClient HTTP_CLIENT = new BasicHttpClient(null, null);

  /**
   *
   * @since 1.0.10
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DISCOVERY_NODE1 = new SpringApplicationBuilder().sources(EurekaServerApplication.class)
        .run("--spring.profiles.active=" + "peer1-development");
    waitMs(5);
    DISCOVERY_NODE2 = new SpringApplicationBuilder().sources(EurekaServerApplication.class)
        .run("--spring.profiles.active=" + "peer2-development");
    waitMs(5);
    // 注册两个客户端
    EUREKA_CLIENTS = startClients(DISCOVERY_NODE1.getEnvironment(), EUREKA_CLIENT1_NAME, EUREKA_CLIENT2_NAME);
    waitMs(15);
  }

  /**
   * @since 1.0.10
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    try {
      closeClients(EUREKA_CLIENTS);
    } catch (Exception e) {
      logger.error("关闭clients出错", e);
    }
    try {
      DISCOVERY_NODE1.close();
    } catch (Exception e) {}
    try {
      DISCOVERY_NODE2.close();
    } catch (Exception e) {}
  }

  /**
   * 测试方案：
   * </p>
   * 1，检测ServerCodecProxy是否替换了ServerCodecs（可获取bean，判断className）
   * </p>
   * 2，junit启动两个eureka server节点： http://localhost:8761,http://localhost:8762，</br>
   * 请求以上泄漏token的接口，检查是否存在META_CLIENT_TOKEN（不可存在）；</br>
   * 可new两个SpringBootApplication，然后使用dummy-eureka-client注册几个客户端来实现</br>
   * 3，junit分别调用两个SpringBoot的DiscoveryClient,检查META_CLIENT_TOKEN是否存在（一定要有，双节点都要有）
   * </p>
   * 4，检查接口：PUT /eureka/apps/{vip}/{instanceId}/metadata是否能访问
   * </p>
   * 5，断言eureka-core里的com.netflix.eureka.resources里接口方法的签名是否变动
   * </p>
   * 
   * @see https://github.com/Netflix/eureka/wiki/Eureka-REST-operations
   */
  @Test
  public void securityCheckpoint() {
    // 1,编码器是否被我们替换了
    testServerCodecProxy();
    waitMs(6);
    Errors errors = new Errors();
    // 2，token有泄露
    testLeakTokenToClients(errors);
    waitMs(5);
    // 4，检查接口：PUT /eureka/apps/{vip}/{instanceId}/metadata是否能访问
    testPutMetadataDisabled(errors);
    // 5，检查eureka-core是否有变动
    testEurekaCoreProvidedResourceChanged(errors);
    if (!errors.get().isEmpty()) {
      logger.error("以下为错误原因：");
      for (String error : errors.get()) {
        logger.error(error);
      }
    }
    Assert.assertTrue("校验未通过", errors.get().isEmpty());
  }

  /**
   * 3，junit分别调用两个SpringBoot的DiscoveryClient,检查META_CLIENT_TOKEN是否存在（一定要有，双节点都要有）
   */
  @Test
  public void checkEurekaServerExistTokens() {
    Errors errors = new Errors();
    PeerAwareInstanceRegistry discoveryServer1 =
        DISCOVERY_NODE1.getBean(PeerAwareInstanceRegistry.class);
    PeerAwareInstanceRegistry discoveryServer2 =
        DISCOVERY_NODE2.getBean(PeerAwareInstanceRegistry.class);
    final String client1Name = (CLIENT_PREFIX + EUREKA_CLIENT1_NAME).toUpperCase();
    final String client2Name = (CLIENT_PREFIX + EUREKA_CLIENT2_NAME).toUpperCase();
    waitMs(5);
    // 3，开始校验服务发现自己是否有安全token，没有被我们的实现弄丢
    discoveryServerTokenExistsCheck(discoveryServer1.getApplication(client1Name),
        "peer1-" + client1Name, errors);
    discoveryServerTokenExistsCheck(discoveryServer1.getApplication(client2Name),
        "peer1-" + client2Name, errors);
    
    waitMs(5);
    discoveryServerTokenExistsCheck(discoveryServer2.getApplication(client1Name),
        "peer2-" + client1Name, errors);
    discoveryServerTokenExistsCheck(discoveryServer2.getApplication(client2Name),
        "peer2-" + client2Name, errors);

    if (!errors.get().isEmpty()) {
      logger.error("以下为错误原因：");
      for (String error : errors.get()) {
        logger.error(error);
      }
    }
    Assert.assertTrue("校验未通过", errors.get().isEmpty());
  }

  /**
   * 1，检测ServerCodecProxy是否替换了ServerCodecs（可获取bean，判断className）
   * </p>
   */
  private void testServerCodecProxy() {
    ServerCodecs codecs1 = DISCOVERY_NODE1.getBean(ServerCodecs.class);
    assertNotNull(
        String.format("Peer1 ServerCodecs 未被其换成：%s",
            ServerCodecProxy.DelegateServerCodecs.class),
        codecs1 != null
            && codecs1.getClass() == ServerCodecProxy.DelegateServerCodecs.class);

    ServerCodecs codecs2 = DISCOVERY_NODE2.getBean(ServerCodecs.class);
    assertNotNull(
        String.format("Peer2 ServerCodecs 未被其换成：%s",
            ServerCodecProxy.DelegateServerCodecs.class),
        codecs2 != null
            && codecs2.getClass() == ServerCodecProxy.DelegateServerCodecs.class);
  }

  /**
   * 2，junit启动两个eureka server节点： http://localhost:8761,http://localhost:8762，</br>
   * 请求以上泄漏token的接口，检查是否存在META_CLIENT_TOKEN（不可存在）；</br>
   * 可new两个SpringBootApplication，然后使用dummy-eureka-client注册几个客户端来实现</br>
   * 
   * @see https://github.com/Netflix/eureka/wiki/Eureka-REST-operations
   */
  private void testLeakTokenToClients(Errors errors) {
    /// eureka/apps,/eureka/apps/{vip},/eureka/vips/{vip},/eureka/svips/{svip};
    // 服务发现地址
    String peer1ServiceUrl = DISCOVERY_NODE1.getEnvironment()
        .getProperty("eureka.client.serviceUrl.defaultZone");
    String peer2ServiceUrl = DISCOVERY_NODE2.getEnvironment()
        .getProperty("eureka.client.serviceUrl.defaultZone");
    String client1Vip = CLIENT_PREFIX + EUREKA_CLIENT1_NAME;
    String client2Vip = CLIENT_PREFIX + EUREKA_CLIENT2_NAME;
    // peer1 json
    tokenLeakDetect(peer1ServiceUrl + "apps", "application/json", errors);
    tokenLeakDetect(peer1ServiceUrl + "apps/" + client1Vip,
        "application/json", errors);
    tokenLeakDetect(peer1ServiceUrl + "vips/" + client2Vip,
        "application/json", errors);
    // peer1 xml
    tokenLeakDetect(peer1ServiceUrl + "apps", "application/xml", errors);
    tokenLeakDetect(peer1ServiceUrl + "apps/" + client1Vip,
        "application/xml", errors);
    tokenLeakDetect(peer1ServiceUrl + "vips/" + client2Vip,
        "application/xml", errors);
    // peer2 json
    tokenLeakDetect(peer2ServiceUrl + "apps", "application/json", errors);
    tokenLeakDetect(peer2ServiceUrl + "apps/" + client1Vip,
        "application/json", errors);
    tokenLeakDetect(peer2ServiceUrl + "vips/" + client2Vip,
        "application/json", errors);
    // peer2 xml
    tokenLeakDetect(peer2ServiceUrl + "apps", "application/xml", errors);
    tokenLeakDetect(peer2ServiceUrl + "apps/" + client1Vip,
        "application/xml", errors);
    tokenLeakDetect(peer2ServiceUrl + "vips/" + client2Vip,
        "application/xml", errors);

    String ipAddress = getIpAddress();
    String client1InstanceId = CLIENT_PREFIX + ipAddress;
    String client2InstanceId = CLIENT_PREFIX + ipAddress;
    // 其他接口/eureka/instances/{instanceId},/eureka/apps/{vip}/{instanceId};
    tokenLeakDetect(peer1ServiceUrl + "instances/" + client1InstanceId, "application/json", errors);
    tokenLeakDetect(peer1ServiceUrl + "instances/" + client2InstanceId, "application/xml", errors);
    tokenLeakDetect(peer1ServiceUrl + "apps/" + client1Vip + "/" + client1InstanceId, "application/json", errors);
    tokenLeakDetect(peer1ServiceUrl + "apps/" + client2Vip + "/" + client2InstanceId, "application/xml", errors);

    tokenLeakDetect(peer2ServiceUrl + "instances/" + client1InstanceId, "application/json", errors);
    tokenLeakDetect(peer2ServiceUrl + "instances/" + client2InstanceId, "application/xml", errors);
    tokenLeakDetect(peer2ServiceUrl + "apps/" + client1Vip + "/" + client1InstanceId, "application/json", errors);
    tokenLeakDetect(peer2ServiceUrl + "apps/" + client2Vip + "/" + client2InstanceId, "application/xml", errors);
  }

  /**
   * 4，检查接口：PUT /eureka/apps/{vip}/{instanceId}/metadata是否能访问
   */
  private void testPutMetadataDisabled(Errors errors) {
    String peer1ServiceUrl = DISCOVERY_NODE1.getEnvironment()
        .getProperty("eureka.client.serviceUrl.defaultZone");
    String peer2ServiceUrl = DISCOVERY_NODE2.getEnvironment()
        .getProperty("eureka.client.serviceUrl.defaultZone");
    String client1Vip = CLIENT_PREFIX + EUREKA_CLIENT1_NAME;
    String client2Vip = CLIENT_PREFIX + EUREKA_CLIENT2_NAME;
    // /eureka/apps/appID/instanceID/metadata;
    String ipAddress = getIpAddress();
    String client1InstanceId = CLIENT_PREFIX + ipAddress;
    String client2InstanceId = CLIENT_PREFIX + ipAddress;
    this.testMetadataPutDisabled(peer1ServiceUrl + "apps/" + client1Vip + "/" + client1InstanceId + "/metadata", errors);
    this.testMetadataPutDisabled(peer1ServiceUrl + "apps/" + client2Vip + "/" + client2InstanceId + "/metadata", errors);

    this.testMetadataPutDisabled(peer2ServiceUrl + "apps/" + client1Vip + "/" + client1InstanceId + "/metadata", errors);
    this.testMetadataPutDisabled(peer2ServiceUrl + "apps/" + client2Vip + "/" + client2InstanceId + "/metadata", errors);
  }

  /**
   * 5，断言eureka-core里的com.netflix.eureka.resources里接口方法的签名是否变动<br>
   * 目前实现仅检查com.netflix.eureka.resources类文件的大小<br>
   * 如果resources接口有调整，应该修改测试用例，以确保安全测试覆盖到所有点.<br>
   * 请确保eureka新版本新增的接口不能泄露token。<br>
   * 当前测试基准Eureka版本为：eureka-core-1.4.12
   */
  private void testEurekaCoreProvidedResourceChanged(Errors errors) {
    String packageName = "com.netflix.eureka.resources";
    Map<String, String> classVersionMap = new HashMap<>();
    // eureka-core-1.4.12
    classVersionMap.put("SecureVIPResource", "b5bcf15fabffe2852d8c4f340496c0b6");
    classVersionMap.put("ASGResource", "fdee41b1ccc0709d6e958d2976e0d25e");
    classVersionMap.put("InstanceResource", "55401b2bb626bceeb3ad134a1c5a9800");
    classVersionMap.put("VIPResource", "9c93d0ab3e80002629b5580a1659338f");
    classVersionMap.put("DefaultServerCodecs", "17bca68c5cd6ec2885c54f7a3ebcb2b5");
    classVersionMap.put("StatusResource", "4406c79ae2ba6fb2b1b6fbc3403e95fe");
    classVersionMap.put("ServerCodecs", "d802ca09156f87247f5b0b2ef29f44f8");
    classVersionMap.put("AbstractVIPResource", "cb80cc9f5c652b1108b39fce296e561f");
    classVersionMap.put("ApplicationResource", "9a689a6171be019fc70c37c5ce601a1a");
    classVersionMap.put("InstancesResource", "448921773427eb83e868eb468df44b24");
    classVersionMap.put("ServerInfoResource", "33758c839a1e55dbc806be854165ce3d");

    // 如果新增文件，可能检测不到。
    for (String clz : classVersionMap.keySet()) {
      String className = packageName + "." + clz;
      try {
        String version = getCurrentVersion(className);
        String oldVersion = classVersionMap.get(clz);
        if (version.equals(oldVersion)) {
          logger.info("resource pass {} =====> 没有变更", className);
        } else {
          errors.addError(String.format("resouce有变动，class:%s,version:%s，previous version:%s，请检查具体实现上的区别", className, version, oldVersion));
        }
      } catch (ClassNotFoundException e) {
        errors.addError(String.format("resouce有变动，class:%s not found，请检查eureka-core", className));
      }
    }
  }

  private String getCurrentVersion(String className) throws ClassNotFoundException {
    Class<?> classzz = Thread.currentThread().getContextClassLoader().loadClass(className);
    StringBuilder sb = new StringBuilder(200);
    Method[] methods = classzz.getDeclaredMethods();
    sb.append(className);
    if (methods != null && methods.length > 0) {
      // 必须给方法排序，不然，可能每次md5值会变
      Arrays.sort(methods, Comparator.comparing(Method::getName));
      for (Method method : methods) {
        if (method.isBridge() || method.isSynthetic() || Modifier.isPrivate(method.getModifiers())) {
          continue;
        }
        sb.append(";").append(method.toString());
      }
    }
    return Hashing.md5().hashBytes(sb.toString().getBytes())
        .toString();
  }

  private String getIpAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return null;
    }
  }

  private void discoveryServerTokenExistsCheck(
      com.netflix.discovery.shared.Application application, String context, Errors errors) {
    if (application == null || application.getInstances().isEmpty()) {
      if (errors == null) {
        logger.info("discovery server 没有此服务:" + context);
      } else {
        errors.addError("discovery server 没有此服务:" + context);
      }
      return;
    }
    List<InstanceInfo> instances = application.getInstances();
    for (InstanceInfo node : instances) {
      if (!node.getMetadata().containsKey(ServerCodecProxy.META_CLIENT_TOKEN)) {
        if (errors == null) {
          logger.info(String.format("不存在安全token =====> instance(vip=%s,id=%s,context=%s)",
              node.getVIPAddress(), node.getInstanceId(), context));
        } else {
          errors.addError(String.format("不存在安全token =====> instance(vip=%s,id=%s,context=%s)",
              node.getVIPAddress(), node.getInstanceId(), context));
        }
      } else {
        logger.info("包含安全token pass:{} ====>> instance(vip={},id={},context={})",
            node.getMetadata().get(ServerCodecProxy.META_CLIENT_TOKEN),
            node.getVIPAddress(), node.getInstanceId(), context);
      }
    }

  }

  /**
   * 测试url是否被禁止访问
   * 
   * @param errors
   */
  private void testMetadataPutDisabled(String forbiddenUrl, Errors errors) {
    Request request = new Request("PUT", forbiddenUrl,
        ImmutableMap.of(), null, StandardCharsets.UTF_8);
    try {
      Response response = HTTP_CLIENT.execute(request, new Request.Options(3000, 3000));
      if (response == null) {
        errors.addError(
            String.format("access forbidden error, url:%s，response is null",
                forbiddenUrl));
      }
      if (response != null) {
        String content = readAndClose(response);
        if (response.status() != 403) {
          // 响应不是200
          errors.addError(String.format(
              "access forbidden error, url:%s，status:%s，response:%s",
              forbiddenUrl, response.status(), content));
        } else {
          logger.info("access forbidden pass ==>> {}，response:{}",
              forbiddenUrl, content);
        }
      }
    } catch (IOException e) {
      errors.addError(String.format("forbidden access error, url:%s，message:%s",
          forbiddenUrl, e.getMessage()));
    }
  }

  /**
   * token泄漏测试
   */
  private void tokenLeakDetect(String leakTokenUrl, String accept, final Errors errors) {
    Request request = new Request("GET", leakTokenUrl,
        ImmutableMap.of("accept", Arrays.asList(accept)), null, StandardCharsets.UTF_8);
    try {
      Response response = HTTP_CLIENT.execute(request, new Request.Options(3000, 3000));
      if (response == null) {
        errors.addError(
            String.format("token leak detect error, url:%s，accept:%s，response is null",
                leakTokenUrl, accept));
      }
      if (response != null) {
        String content = readAndClose(response);
        if (response.status() != 200 || content == null) {
          // 响应不是200
          errors.addError(String.format(
              "token leak detect error, url:%s，accept:%s,status:%s，response:%s",
              leakTokenUrl, accept, response.status(), content));
        } else {
          // 返回内容包含token关键字
          if (content.contains(ServerCodecProxy.META_CLIENT_TOKEN)) {
            errors.addError(String.format(
                "token leak,response contains: %s, url:%s，accept:%s,status:%s，response:%s",
                ServerCodecProxy.META_CLIENT_TOKEN, leakTokenUrl, accept,
                response.status(), content));
          } else {
            logger.info("tokenLeakDetect pass ==>> {}，accept:{}，response:",
                leakTokenUrl, accept);
            logger.info(content);
          }
        }
      }
    } catch (IOException e) {
      errors.addError(String.format("token leak detect error, url:%s，accept:%s，message:%s",
          leakTokenUrl, accept, e.getMessage()));
    }
  }



  /**
   * 等待多少毫秒
   */
  private static void waitMs(int timeInMs) {
    try {
      TimeUnit.SECONDS.sleep(timeInMs);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static List<DiscoveryClient> startClients(Environment env, String... clientNames) {
    List<DiscoveryClient> clients = new ArrayList<>(clientNames.length);
    for (String client : clientNames) {
      // 启动客户端去注册
      DiscoveryClient testClient1 = new DiscoveryClient("localjunit", client);
      try {
        Field field = DiscoveryClient.class.getDeclaredField("environment");
        field.setAccessible(true);
        field.set(testClient1, env);
        testClient1.afterPropertiesSet();
      } catch (Exception e) {
        e.printStackTrace();
      }
      clients.add(testClient1);
    }
    return clients;
  }

  private static void closeClients(List<DiscoveryClient> testClients) {
    for (DiscoveryClient discoveryClient : testClients) {
      try {
        discoveryClient.shutdown();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String readAndClose(Response response) {
    if (response != null) {
      Body body = response.body();
      if (body == null) {
        return null;
      }
      // auto close
      try (Reader reader = body.asReader();) {
        if (reader == null) {
          return null;
        }
        StringWriter writer = new StringWriter(200);
        char[] buffer = new char[2048];
        int len = -1;
        while ((len = reader.read(buffer)) > 0) {
          writer.write(buffer, 0, len);
        }
        return writer.toString();
      } catch (Exception e) {}
    }
    return null;
  }

  static class Errors {
    private final List<String> errors;

    Errors() {
      errors = new ArrayList<>(20);
    }

    Errors addError(final String error) {
      this.errors.add(error);
      return this;
    }

    /**
     * never return null
     */
    List<String> get() {
      return this.errors;
    }

  }

}


