package com.fsocial.services.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OnlineServiceImpl {
    StringRedisTemplate redisTemplate;
    String USER_ONLINE_PREFIX = "user:online:";
    Duration ONLINE_TTL = Duration.ofSeconds(60);

    public String key(String userId) {
        return USER_ONLINE_PREFIX + userId;
    }

    public void setOnline(String userId) {
        String key = key(userId);
        redisTemplate.opsForValue().setBit(key, 0, true);
        redisTemplate.expire(key, ONLINE_TTL);
    }

    public void setOffline(String userId) {
        String key = key(userId);
        redisTemplate.delete(key);
    }

    public boolean isOnline(String userId) {
        String key = key(userId);
        Boolean bit = redisTemplate.opsForValue().getBit(key, 0);

        return Boolean.TRUE.equals(bit);
    }

    public Map<String, Boolean> isOnlineBatch(List<String> userIds) {
        List<Object> result = redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (String userId : userIds) {
                connection.stringCommands().getBit(key(userId).getBytes(), 0);
            }
            return null;
        });

        Map<String, Boolean> map = new HashMap<>();

        for (int i = 0; i < userIds.size(); i++) {
            map.put(userIds.get(i), Boolean.TRUE.equals(result.get(i)));
        }

        return map;
    }
}
