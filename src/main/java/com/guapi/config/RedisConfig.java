package com.guapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String ,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //配置序列化方式，最终要将数据存到数据库
        //设置key序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置哈希序列化（key:value）方式
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        //生效设置
        template.afterPropertiesSet();
        return template;
    }
}
