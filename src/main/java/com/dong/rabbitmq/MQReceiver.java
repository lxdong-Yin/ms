package com.dong.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dong.domain.MiaoshaOrder;
import com.dong.domain.MiaoshaUser;
import com.dong.redis.RedisService;
import com.dong.service.GoodsService;
import com.dong.service.MiaoshaService;
import com.dong.service.OrderService;
import com.dong.vo.GoodsVo;

@Service
public class MQReceiver {

	private static Logger log = LoggerFactory.getLogger(MQSender.class);
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@RabbitListener(queues=MQConfiguration.MIAOSHA_QUEUE)
	public void receive(String message) {
		log.info("receive message:"+message);
		MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();
		
		GoodsVo goods = goodsService.getGoodsByGoodsId(goodsId);
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return;
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return;
    	}
    	//减库存 下订单 写入秒杀订单
    	miaoshaService.miaosha(user, goods);
	}
	
	
	
	
	
	
	
	/*@RabbitListener(queues=MQConfiguration.QUEUE)
	public  void receiveMessage(String message) {
		log.info("receive:"+message);
	}
	
	@RabbitListener(queues=MQConfiguration.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info(" topic  queue1 message:"+message);
	}
	
	@RabbitListener(queues=MQConfiguration.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info(" topic  queue2 message:"+message);
	}
	
	@RabbitListener(queues=MQConfiguration.HEADER_QUEUE)
	public void receiveHeaderQueue(byte[] message) {
		log.info(" header  queue message:"+new String(message));
	}*/
	
	
}
