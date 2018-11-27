package com.dong.access;

import java.io.OutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.dong.domain.MiaoshaUser;
import com.dong.redis.AccessKey;
import com.dong.redis.RedisService;
import com.dong.result.CodeMsg;
import com.dong.service.MsUserService;

public class AccessInterceptor extends HandlerInterceptorAdapter{

	
	@Autowired
	MsUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(handler instanceof HandlerMethod) {
			MiaoshaUser user = getUser(request,response);
			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if(accessLimit == null) {
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if(needLogin) {
				if(user == null) {
					render(response,CodeMsg.SESSION_ERROR);
					return false;
				}
				key += "_" + user.getId();
			}else {
				//不需要操作
			}
			
			AccessKey ak = AccessKey.withExpire(seconds);
			Integer count = redisService.get(ak, key, Integer.class);
			if(count == null) {
				redisService.set(ak, key, 1);
			}else if(count < maxCount) {
				redisService.incr(ak, key);
			}else {
				render(response,CodeMsg.ACCESS_LIMIT_REACHED);
			}
		}
		
		return true;
	}
	
	
	
	private void render(HttpServletResponse response, CodeMsg msg) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out= response.getOutputStream();
		String str = JSON.toJSONString(msg);
		out.write(str.getBytes("UTF-8"));
		out.flush();
		out.close();
			
	}



	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(MsUserService.COOKI_NAME_TOKEN);
		String cookieToken = getCookieValue(request, MsUserService.COOKI_NAME_TOKEN);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}
	
	private String getCookieValue(HttpServletRequest request, String cookiName) {
		Cookie[]  cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookiName)) {
				return cookie.getValue();
			}
		}
		return null;
	}
	

}
