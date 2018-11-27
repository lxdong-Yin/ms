package com.dong.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.alibaba.druid.util.StringUtils;
import com.dong.domain.MiaoshaUser;
import com.dong.redis.GoodsKey;
import com.dong.redis.RedisService;
import com.dong.result.Result;
import com.dong.service.GoodsService;
import com.dong.vo.GoodsDetailVo;
import com.dong.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	GoodsService goodsService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	
	/**
	 * 做页面缓存
	 * @param model
	 * @param user
	 * @return
	 */
	@RequestMapping(value="/toList",produces="text/html")
	@ResponseBody
	public String list(HttpServletRequest request, HttpServletResponse response,Model model,MiaoshaUser user) {
		model.addAttribute("user", user);
		
		//取缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		
		List<GoodsVo> goodsList = goodsService.getGoodsList();
		model.addAttribute("goodsList", goodsList);
		
		//如果缓存中没有，则重新渲染模板
		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
				model.asMap(), applicationContext);
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}
	
	@RequestMapping(value = "/toDetail2/{goodsId}",produces="text/html")
	@ResponseBody
	public String detail2(HttpServletRequest request, HttpServletResponse response,Model model,MiaoshaUser user,@PathVariable("goodsId") long goodsId) {
		model.addAttribute("user", user);
		
		//取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		
		
		GoodsVo goods = goodsService.getGoodsByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		long startTime = goods.getStartDate().getTime();
		long endTime = goods.getEndDate().getTime();
		long nowTime = System.currentTimeMillis();
		
		int miaoshaStatus = 0;
		int remainSeconds = 0;
		
		if(nowTime < startTime) {
			//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int) ((startTime - nowTime)/1000);
		}else if(nowTime > endTime) {
			//秒杀结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {
			//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		
		//手动渲染
		//如果缓存中没有，则重新渲染模板
				SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
						model.asMap(), applicationContext);
				//手动渲染
				html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
				if(!StringUtils.isEmpty(html)) {
					redisService.set(GoodsKey.getGoodsList, ""+goodsId, html);
				}
		
		return html;
	}
	
	@RequestMapping(value = "/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response,Model model,MiaoshaUser user,@PathVariable("goodsId") long goodsId) {
		model.addAttribute("user", user);
			
		GoodsVo goods = goodsService.getGoodsByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		long startTime = goods.getStartDate().getTime();
		long endTime = goods.getEndDate().getTime();
		long nowTime = System.currentTimeMillis();
		
		int miaoshaStatus = 0;
		int remainSeconds = 0;
		
		if(nowTime < startTime) {
			//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int) ((startTime - nowTime)/1000);
		}else if(nowTime > endTime) {
			//秒杀结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {
			//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		
		GoodsDetailVo vo = new GoodsDetailVo();
    	vo.setGoods(goods);
    	vo.setUser(user);
    	vo.setRemainSeconds(remainSeconds);
    	vo.setMiaoshaStatus(miaoshaStatus);
    	return Result.success(vo);
	}
	
}
