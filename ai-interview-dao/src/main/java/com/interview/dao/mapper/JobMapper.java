package com.interview.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.dao.entity.Job;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
