package com.ticket.redis.config;

/**
 * Redis key 统一管理
 * 过期监听 key 格式：ticket:order:{eventId}:{ticketId}:{userId}:{orderNo}
 * 每次抢票 orderNo 含时间戳+随机数，天然不重复
 */
public class RedisKeyBuild { //为啥要final？

    //存库存key
    public static String stockKey(Long eventId, Long ticketId){
        return String.format("ticket:stock:%d:%d", eventId, ticketId);
    }
    // 查重 set key（用于去重）
    public static String dedupeKey(Long userId, Long eventId){
        return String.format("ticket:dedupe:%d:%d", userId, eventId);
    }
    // 过期监听 key（TTL 900s，key 过期触发库存回补）
    public static String expireWatchKey(Long eventId, Long ticketId, Long userId, String orderNo) {
        return String.format("ticket:order:%d:%d:%d:%s",eventId, ticketId, userId, orderNo);
    }

    /**
     * 从过期 key 解析 orderNo（最后一段）
     * ticket:order:{eventId}:{ticketId}:{userId}:{orderNo}
     */
    public static String parseOrderNo(String expiredkey){
        String[] parse = expiredkey.split(":");
        return parse[parse.length - 1];
    }
    /**
     * 判断是否为抢票过期监听 key todo?
     */
    public static boolean isTicketOrderKey(String key){
        return key != null && key.startsWith("ticket:order:");
    }
}
