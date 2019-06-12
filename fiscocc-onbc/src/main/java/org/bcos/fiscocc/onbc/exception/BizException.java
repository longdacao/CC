package org.bcos.fiscocc.onbc.exception;

/**
 * <pre>
 * *********************************************
 * Copyright.
 * All rights reserved.
 * Description:
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2017年7月17日
 * *********************************************
 * </pre>
 */
public class BizException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private Integer code;
	
	public BizException() {
	} 

	public BizException(String message) {
		super(message);
	}
	
	public BizException(String message, Integer code) {
		super(message);
		this.code = code;
	}
	
	public BizException(String message,Throwable cause) {
		super(message, cause);
	}
	
	public BizException(String message, Integer code, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
}
