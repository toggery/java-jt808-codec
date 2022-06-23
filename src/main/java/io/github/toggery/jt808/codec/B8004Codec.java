package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8004;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8004 查询服务器时间应答】编码解码器 // 2019 new
 *
 * @author togger
 */
public final class B8004Codec implements Codec<B8004> {

    private B8004Codec() {}

    /** 单例 */
    public static final B8004Codec INSTANCE = new B8004Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8004 target) {
        Codec.writeBcd(buf, target.getTime(), 6);
    }

    @Override
    public void decode(int version, ByteBuf buf, B8004 target) {
        target.setTime(Codec.readBcd(buf, 6, false));
    }

    @Override
    public B8004 newInstance() {
        return new B8004();
    }

}
