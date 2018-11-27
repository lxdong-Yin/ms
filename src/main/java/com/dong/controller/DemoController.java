package com.dong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dong.domain.User;
import com.dong.rabbitmq.MQSender;
import com.dong.redis.RedisService;
import com.dong.redis.UserKey;
import com.dong.result.Result;
import com.dong.service.UserService;

@Controller
@RequestMapping("/demo")
public class DemoController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	MQSender mqSender;
	
	
	/*@RequestMapping("/mqs")
 	@ResponseBody
 	public Result<Boolean> send() {
        mqSender.sendHeader("哈喽，您好");
        return  Result.success(true);
    }*/

	@RequestMapping("/")
 	@ResponseBody
    String home() {
        return "Hello World!";
    }
 	
 	@RequestMapping("/thymeleaf")
    public String  thymeleaf(Model model) {
 		model.addAttribute("name", "Joshua");
 		return "hello";
    }
 	
 	 
    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
    	User user = userService.getById(1);
        return Result.success(user);
    }
    
    
    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
    	userService.tx();
        return Result.success(true);
    }
 	
    
    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
    	User  user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }
    
    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
    	User user  = new User();
    	user.setId(1);
    	user.setName("1111");
    	redisService.set(UserKey.getById, ""+1, user);//UserKey:id1
        return Result.success(true);
    }
    
    
}
