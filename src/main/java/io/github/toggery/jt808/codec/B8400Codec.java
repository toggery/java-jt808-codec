package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8400;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8400 电话回拨】编码解码器
 *
 * @author togger
 */
public final class B8400Codec implements Codec<B8400> {

    private B8400Codec() {}

    /** 单例 */
    public static final B8400Codec INSTANCE = new B8400Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8400 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeString(buf, target.getPhone());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8400 target) {
        target.setType(Codec.readByte(buf));
        target.setPhone(Codec.readString(buf));
    }

    @Override
    public B8400 newInstance() {
        return new B8400();
    }

}
