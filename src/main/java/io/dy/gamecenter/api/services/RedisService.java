package io.dy.gamecenter.api.services;

import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Resource(name = "redisTemplate")
    private HashOperations hashOperations;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueObjectOperations;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Boolean> valueBooleanOperations;

    private Gson gson = new Gson();

    public boolean checkIfKeyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean checkIfKeyExists(String key, String hashKey) {
        return hashOperations.hasKey(key, hashKey);
    }

    public void saveObjectWithExpiredTime(String key, Object value, Duration duration) {
        valueObjectOperations.set(key, value);
        redisTemplate.expire(key, duration);
    }

    public void saveObjectWithExpiredTime(String key, String hashKey, Object value, Duration duration) {
        hashOperations.put(key, hashKey, value);
        redisTemplate.expire(key, duration);
    }

    public void saveObjectWithoutExpiredTime(String key, Object value) {
        valueObjectOperations.set(key, value);
    }

    public void saveObjectWithoutExpiredTime(String key, String hashKey, Object value) {
        hashOperations.put(key, hashKey, value);
    }

    public Object getObject(String key) {
        return valueObjectOperations.get(key);
    }

    public Object getObject(String key, String hashKey) {
        return hashOperations.get(key, hashKey);
    }

    public void lock(String key, Duration duration) {
        valueBooleanOperations.set(key, true);
        redisTemplate.expire(key, duration);
    }

    public void release(String key) {
        redisTemplate.delete(key);
    }

    public Set<ZSetOperations.TypedTuple<String>> getLeaderboard(String key, int start, int end) {
        return zSetOperations.reverseRangeWithScores(key, start, end);
    }

    public void saveLeaderboard(String key, String userId, int score, Duration duration) {
        if (!checkIfKeyExists(key)) {
            zSetOperations.add(key, userId, score);
            redisTemplate.expire(key, duration);
        } else {
            zSetOperations.add(key, userId, score);
        }
    }
}
