package com.dong.exception;

import com.dong.result.CodeMsg;


public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg codeMsg;
	
	public GlobalException(CodeMsg codeMsg) {
		this.codeMsg = codeMsg;
	}
	
	public CodeMsg getCm() {
		return codeMsg;
	}
}
