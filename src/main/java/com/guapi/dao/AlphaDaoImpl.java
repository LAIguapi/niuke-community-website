package com.guapi.dao;

import org.springframework.stereotype.Repository;

//扫描数据库的注解
@Repository
public class AlphaDaoImpl implements AlphaDao{
    @Override
    public String select() {
        return "AlphaDaoImpl";
    }
}
