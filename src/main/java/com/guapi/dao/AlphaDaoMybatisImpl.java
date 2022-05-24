package com.guapi.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("Mybatis")
@Primary
//调用时优先装配
public class AlphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "AlphaDaoMybatisImpl";
    }
}
