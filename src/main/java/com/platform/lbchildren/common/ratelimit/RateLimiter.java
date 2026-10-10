package com.platform.lbchildren.common.ratelimit;

/**
 * 防滥用限流器（B3）
 * <p>
 * 只限"行为特征"（频率/突变），阈值远高于人类正常使用；单机内存实现，
 * 将来多实例可通过新的实现平滑替换（接口即替换缝）。
 */
public interface RateLimiter {

    /**
     * 尝试放行一次。
     *
     * @param key 限流 key，如按用户 "uid:123"、全局 "AI_GLOBAL"、同句去重 "dup:123:xxx"
     * @return true=放行；false=超限（应拒绝本次请求）
     */
    boolean allow(String key);
}