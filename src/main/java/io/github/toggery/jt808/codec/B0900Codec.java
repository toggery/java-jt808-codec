package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0900;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0900 数据上行透传
 *
 * @author togger
 */
public final class B0900Codec implements Codec<B0900> {

    private B0900Codec() {}

    /** 单例 */
    public static final B0900Codec INSTANCE = new B0900Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0900 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeBytes(buf, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0900 target) {
        target.setType(Codec.readByte(buf));
        target.setData(Codec.readBytes(buf));
    }

    @Override
    public B0900 newInstance() {
        return new B0900();
    }

}
