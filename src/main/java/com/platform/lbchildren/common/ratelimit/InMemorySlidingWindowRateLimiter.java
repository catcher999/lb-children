package com.platform.lbchildren.common.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单机内存滑动窗口限流实现（B3）
 * <p>
 * 用 ConcurrentHashMap&lt;key, Deque&lt;timestamp&gt;&gt; 维护每个 key 在滑动窗口内的请求时间戳，
 * 超限返回 false。使用 compute 保证单 key 内的 read-modify-write 原子（规避 TOCTOU）。
 * 惰性删除过期时间戳 + @Scheduled 定期清空空 key，避免内存无限增长。
 */
@Slf4j
public class InMemorySlidingWindowRateLimiter implements RateLimiter {

    private final int windowSeconds;
    private final int maxCount;
    private final Map<String, Deque<Long>> store = new ConcurrentHashMap<>();

    public InMemorySlidingWindowRateLimiter(int windowSeconds, int maxCount) {
        this.windowSeconds = windowSeconds;
        this.maxCount = maxCount;
    }

    @Override
    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        long cutoff = now - windowSeconds * 1000L;
        boolean[] allowed = new boolean[1];
        store.compute(key, (k, deque) -> {
            Deque<Long> dq = (deque == null) ? new ArrayDeque<>() : deque;
            while (!dq.isEmpty() && dq.peekFirst() < cutoff) {
                dq.pollFirst();
            }
            if (dq.size() >= maxCount) {
                allowed[0] = false;
                return dq;
            }
            dq.addLast(now);
            allowed[0] = true;
            return dq;
        });
        return allowed[0];
    }

    /** 定期清理空 key，防止无访问的 key 常驻内存 */
    @Scheduled(fixedDelay = 300000)
    public void cleanupEmptyKeys() {
        int before = store.size();
        store.entrySet().removeIf(e -> e.getValue().isEmpty());
        if (store.size() != before) {
            log.debug("限流器清理空 key：{} -> {}", before, store.size());
        }
    }
}