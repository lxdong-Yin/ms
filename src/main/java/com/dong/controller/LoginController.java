package com.dong.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dong.result.Result;
import com.dong.service.MsUserService;
import com.dong.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {

	@Autowired
	MsUserService userService;
	
	private static Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping("/toLogin")
	public String toLogin() {
		return "login";
	}
	
	@RequestMapping("/doLogin")
	@ResponseBody
	public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
		logger.info(loginVo+"");
		//登录
    	userService.login(response, loginVo);
		return Result.success(true);
	}
	
}
