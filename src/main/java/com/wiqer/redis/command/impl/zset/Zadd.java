package com.wiqer.redis.command.impl.zset;


import com.wiqer.redis.RedisCore;
import com.wiqer.redis.command.Command;
import com.wiqer.redis.command.CommandType;
import com.wiqer.redis.command.WriteCommand;
import com.wiqer.redis.datatype.*;
import com.wiqer.redis.resp.BulkString;
import com.wiqer.redis.resp.Resp;
import com.wiqer.redis.resp.RespInt;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class Zadd implements WriteCommand
{
    private BytesWrapper            key;
    private List<ZsetKey> keys;

    @Override
    public CommandType type()
    {
        return CommandType.zadd;
    }

    @Override
    public void setContent(Resp[] array)
    {
        key = ((BulkString) array[1]).getContent();
        keys = new ArrayList<>();
        for (int i = 2; i + 1 < array.length; i += 2)
        {
            long         score  = Long.parseLong(((BulkString) array[i]).getContent().toUtf8String());
            BytesWrapper member = ((BulkString) array[i + 1]).getContent();
            keys.add(new ZsetKey(member, score));
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, RedisCore redisCore)
    {
        RedisData redisData = redisCore.get(key);
        if (redisData == null)
        {
            RedisZset redisZset = new RedisZset();
            int       add       = redisZset.add(keys);
            redisCore.put(key, redisZset);
            RespInt i = RedisBaseData.getRedisDataByType(RespInt.class);
            i.getValue(add);
            ctx.writeAndFlush(i).addListener(future -> i.recovery());

        }
        else if (redisData instanceof RedisZset)
        {
            RedisZset redisZset = (RedisZset) redisData;
            int       add       = redisZset.add(keys);
            RespInt i = RedisBaseData.getRedisDataByType(RespInt.class);
            i.getValue(add);
            ctx.writeAndFlush(i).addListener(future -> i.recovery());
        }
        else
        {
            throw new UnsupportedOperationException("类型不匹配");
        }
    }

    @Override
    public void handle(RedisCore redisCore) {
        RedisData redisData = redisCore.get(key);
        if (redisData == null)
        {
            RedisZset redisZset = new RedisZset();
            int       add       = redisZset.add(keys);
            redisCore.put(key, redisZset);
        }
        else if (redisData instanceof RedisZset)
        {
            RedisZset redisZset = (RedisZset) redisData;
            int       add       = redisZset.add(keys);
        }
        else
        {
            throw new UnsupportedOperationException("类型不匹配");
        }
    }
}
