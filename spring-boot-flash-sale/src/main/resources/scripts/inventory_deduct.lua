-- KEYS[1] = inventory key (vd: "inventory:event:1")
-- KEYS[2] = user booking set key (vd: "booked:event:1")
-- ARGV[1] = quantity cần trừ
-- ARGV[2] = user_id

local inventory = tonumber(redis.call('GET',KEYS[1]))
if inventory == nil then
    return -1 -- Event không tồn tại hoặc chưa load lên Redis
end
if inventory <= 0 then
    return -2 -- Hết hàng
end

-- Check user đã book chưa (chống 1 user book nhiều lần bằng spam click)
local alreadyBooked = redis.call('SISMEMBER', KEYS[2], ARGV[2])
if alreadyBooked == 1 then
    return -3 -- User đã đặt trước đó rồi
end

-- Trừ inventory và ghi nhận user đã book (atomic)
redis.call('DECRBY', KEYS[1], ARGV[1])
redis.call('SADD', KEYS[2], ARGV[2])--SADD: Thêm cái user_id này vào tập hợp Set của những người đã mua
return 1 -- Đặt hàng thành công