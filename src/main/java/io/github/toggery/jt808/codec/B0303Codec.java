package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0303;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0303 信息点播/取消】编码解码器 // 2019 del
 *
 * @author togger
 */
public final class B0303Codec implements Codec<B0303> {

    private B0303Codec() {}

    /** 单例 */
    public static final B0303Codec INSTANCE = new B0303Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0303 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeByte(buf, target.getAction());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0303 target) {
        target.setType(Codec.readByte(buf));
        target.setAction(Codec.readByte(buf));
    }

    @Override
    public B0303 newInstance() {
        return new B0303();
    }

}
