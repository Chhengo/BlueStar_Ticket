-- KEYS[1] = ticket:stock:{eventId}:{ticketId}
-- 返回值：1=扣减成功 0=库存不足

local stock = tonumber(redis.call("GET",KEYS[1]))
--获取当前库存 如果库存为null或者已经为0 不允许扣减
if stock == nil or stock <= 0 then
    return 0
end
-- redis唤起自减函数给库存-1
redis.call(DECR,KEYS[1])
--stock = DECR(stock)
return 1