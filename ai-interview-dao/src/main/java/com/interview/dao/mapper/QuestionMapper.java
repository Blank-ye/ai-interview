package com.interview.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.dao.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
