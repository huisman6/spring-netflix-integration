package com.youzhixu.api.model;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月16日 下午3:12:50
 * @since 1.0.0
 * @Copyright (c) 2015, Youzhixu.com All Rights Reserved.
 */

public class City implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer gbCode;
	private String name;
	private double x;
	private double y;

	public City() {
		super();
	}

	public City(Integer gbCode, String name, double x, double y) {
		super();
		this.gbCode = gbCode;
		this.name = name;
		this.x = x;
		this.y = y;
	}


	public Integer getGbCode() {
		return gbCode;
	}

	public void setGbCode(Integer gbCode) {
		this.gbCode = gbCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "City [gbCode=" + gbCode + ", name=" + name + ", x=" + x + ", y=" + y + "]";
	}

}
