package com.youzhixu.api.result;

import com.dooioo.se.lorik.spi.view.support.ErrorResult;

/**
 * 
 * @author huisman
 * @since 1.0.10
 * @Copyright (c) 2016,Youzhixu.com Rights Reserved.
 */
public interface AppErrorCode {
	ErrorResult CITY_NOT_FOUND = new ErrorResult(20000, "CITY NOT FOUND");

	static enum Test {
		NO_NT(2000);
		private int code;

		/**
		 * @param code
		 */
		private Test(int code) {
			this.code = code;
		}

		public String detail() {
			return "" + this.code;
		}
	}
}


