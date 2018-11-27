package com.dong.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dong.access.AccessLimit;
import com.dong.domain.MiaoshaOrder;
import com.dong.domain.MiaoshaUser;
import com.dong.rabbitmq.MQSender;
import com.dong.rabbitmq.MiaoshaMessage;
import com.dong.redis.GoodsKey;
import com.dong.redis.RedisService;
import com.dong.result.CodeMsg;
import com.dong.result.Result;
import com.dong.service.GoodsService;
import com.dong.service.MiaoshaService;
import com.dong.service.MsUserService;
import com.dong.service.OrderService;
import com.dong.vo.GoodsVo;


@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{

	@Autowired
	MsUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	MQSender sender;
	
	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();
	
	//进入这个类的时候会回调这个函数
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.getGoodsList();
		if(goodsList == null) {
			return;
		}
		for(GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}
	
	
	
	/**
	 * 秒杀功能
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
    @RequestMapping(value="/{path}/doMiaosha",method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")String goodsId,@PathVariable("path") String path) {
    	
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	
    	//验证path
    	boolean check = miaoshaService.checkPath(user, goodsId, path);
    	if(!check){
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	
    	//内存标记，减少redis访问
    	boolean over = localOverMap.get(Long.parseLong(goodsId));
    	if(over) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//预减库存
    	long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+Long.parseLong(goodsId));//10
    	if(stock < 0) {
    		 localOverMap.put(Long.parseLong(goodsId), true);
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), Long.parseLong(goodsId));
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//入队
    	MiaoshaMessage message = new MiaoshaMessage();
    	message.setGoodsId(Long.parseLong(goodsId));
    	message.setUser(user);
    	sender.sendMiaoshaMessage(message);
    	return Result.success(0);//排队中
    	
    	/*//判断库存
    	GoodsVo goods = goodsService.getGoodsByGoodsId(Long.parseLong(goodsId));
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    	
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), Long.parseLong(goodsId));
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
    
    	return Result.success(orderInfo);*/
    }


    
    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
    	return Result.success(result);
    }

    /***
     * 生成地址
     * @param request
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.POST)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,@RequestParam("verifyCode")int verifyCode){
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
    	if(!check) {
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	String path  =miaoshaService.createMiaoshaPath(user, goodsId);
    	return Result.success(path);
    }
    
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	try {
    		BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.MIAOSHA_FAIL);
    	}
    }
    
	
}
