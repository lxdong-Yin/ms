package com.dong.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.dong.domain.MiaoshaUser;

@Mapper
public interface MsUserDao {

	@Select("select * from ms_user where id = #{id}")
	public MiaoshaUser getById(@Param("id")long id);
	
}
