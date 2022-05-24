package com.guapi.dao;

import com.guapi.entity.User;
import org.apache.ibatis.annotations.Mapper;

//@Repository//可用
@Mapper
//Mybatis的注解
public interface UserMapper {
    User selectById(int id);
    User selectByName(String name);
    User selectByEmail(String email);
    int insertUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,String headerUrl);
    int updatePassword(int id,String password);
}
