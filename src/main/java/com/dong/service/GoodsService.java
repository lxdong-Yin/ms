package com.dong.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dong.dao.GoodsDao;
import com.dong.domain.MiaoshaGoods;
import com.dong.vo.GoodsVo;

@Service
public class GoodsService {

	@Autowired
	GoodsDao goodsDao;
	
	/**
	 * 获取商品列表
	 * @return
	 */
	public List<GoodsVo> getGoodsList() {
		return goodsDao.listGoodsVo();
	}

	/**
	 * 根据商品id获取相应的商品
	 * @param goodsId
	 * @return
	 */
	public GoodsVo getGoodsByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	public boolean reduceStock(GoodsVo goods) {
		MiaoshaGoods g = new MiaoshaGoods();
		g.setGoodsId(goods.getId());
		int stock = goodsDao.reduceStock(g);
		if (stock > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	
}
