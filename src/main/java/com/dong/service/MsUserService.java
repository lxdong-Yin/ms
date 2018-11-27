package com.dong.service;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.dong.dao.MsUserDao;
import com.dong.domain.MiaoshaUser;
import com.dong.exception.GlobalException;
import com.dong.redis.MiaoshaUserKey;
import com.dong.redis.RedisService;
import com.dong.result.CodeMsg;
import com.dong.util.MD5Util;
import com.dong.util.UUIDUtil;
import com.dong.vo.LoginVo;

@Service
public class MsUserService {

	@Autowired
	MsUserDao msUserDao;
	
	@Autowired
	RedisService redisService;
	
	public static final String COOKI_NAME_TOKEN = "token";
	
	/**
	 *  登录功能
	 * @param response
	 * @param loginVo
	 */
	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		String dbPass = user.getPassword();
		String dbSalt = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, dbSalt);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		
		//生成一个uuid
		String token = UUIDUtil.uuid();
		//将生成的随机token放到缓存中
		addCookie(response,token,user);
		return true;
	}

	/**
	 * 根据id获取对象
	 * @param id
	 * @return
	 */
	public MiaoshaUser getById(long id) {
		return msUserDao.getById(id);
	}
	
	/**
	 * 将token和对应的对象存储到redis缓存中
	 * @param response
	 * @param token
	 * @param user
	 */
	private void addCookie(HttpServletResponse response,String token,MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN,token);
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	/**
	 * 根据token获取对象
	 * @param response
	 * @param token
	 * @return
	 */
	public MiaoshaUser getByToken(HttpServletResponse response,String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
	
}
