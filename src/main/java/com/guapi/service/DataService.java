package com.guapi.service;

import com.guapi.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd");

    //将指点的IP计入uv
    public void recordUV(String ip){
        String redisKey = RedisUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }
    //统计日期范围内的uv
    public long calculateUV(Date start,Date end){
        if (start == null || end == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        //整理日期范围内的key
        ArrayList<String> keyList = new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        //时间如果小于end
        while (!calendar.getTime().after(end)){
            String key = RedisUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //获取了日期以内的时间
        String redisKey=RedisUtil.getUVKey(dateFormat.format(start),dateFormat.format(end));

        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }
    //将指定用户计入DAU
    public void recordDAU(int userId){
        String redisKey=RedisUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }
    //统计指定日期内的DAU
    public long calculateDAU(Date start ,Date end){
        if (start == null || end == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        ArrayList<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey=RedisUtil.getDAUKey(dateFormat.format(start),dateFormat.format(end));
                connection.bitOp(
                        RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),
                        keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
