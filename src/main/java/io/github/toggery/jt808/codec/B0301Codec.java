package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0301;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0301 事件报告 // 2019 del
 *
 * @author togger
 */
public final class B0301Codec implements Codec<B0301> {

    private B0301Codec() {}

    /** 单例 */
    public static final B0301Codec INSTANCE = new B0301Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0301 target) {
        Codec.writeByte(buf, target.getId());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0301 target) {
        target.setId(Codec.readByte(buf));
    }

    @Override
    public B0301 newInstance() {
        return new B0301();
    }

}
