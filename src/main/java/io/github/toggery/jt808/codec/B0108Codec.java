package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0108;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0108 终端升级结果通知
 *
 * @author togger
 */
public final class B0108Codec implements Codec<B0108> {

    private B0108Codec() {}

    /** 单例 */
    public static final B0108Codec INSTANCE = new B0108Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0108 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeByte(buf, target.getResult());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0108 target) {
        target.setType(Codec.readByte(buf));
        target.setResult(Codec.readByte(buf));
    }

    @Override
    public B0108 newInstance() {
        return new B0108();
    }

}
