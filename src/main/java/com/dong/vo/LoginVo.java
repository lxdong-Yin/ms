package com.dong.vo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.dong.validator.IsMobile;

public class LoginVo {

	@NotNull
	@IsMobile
	private String mobile;
	
	@NotNull
	@Length(min=32)
	private String password;
	
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
