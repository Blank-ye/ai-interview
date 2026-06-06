package com.interview.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
