package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.services.RedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisServiceImpl implements RedisService {
   RedisTemplate<String, String> redisTemplate;

    // Methods from timelineService
    @Override
    public void viewed(String userId, String value) {
        this.saveList("viewed_post_" + userId, value);
    }

    @Override
    public List<String> getViewed(String userId) {
        return this.getList("viewed_post_" + userId);
    }

    @Override
    public void cleaerViewed(String userId) {
        try {
            redisTemplate.delete("viewed_post_" + userId);
        } catch (Exception e) {
        }
    }

    @Override
    public void viewedFollowing(String userId, String postId) {
        this.saveList("viewed_post_following_" + userId, postId);
    }

    @Override
    public List<String> getViewedFollowing(String userId) {
        return this.getList("viewed_post_following_" + userId);
    }

    @Override
    public void clearViewedFollowing(String userId) {
        try {
            redisTemplate.delete("viewed_post_following_" + userId);
        } catch (Exception e) {
        }
    }

   @Override
    public void saveData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void saveList(String key, String value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public List<String> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    @Override
    public void personalization(String userId, String value) {
         this.saveList("personalization_" + userId, value);
    }

    @Override
    public List<String> getPersonalization(String userId) {
       return this.getList("personalization_" + userId);
    }


}
