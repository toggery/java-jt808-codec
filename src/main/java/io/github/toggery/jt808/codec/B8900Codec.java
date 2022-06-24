package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8900;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8900 数据下行透传
 *
 * @author togger
 */
public final class B8900Codec implements Codec<B8900> {

    private B8900Codec() {}

    /** 单例 */
    public static final B8900Codec INSTANCE = new B8900Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8900 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeBytes(buf, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8900 target) {
        target.setType(Codec.readByte(buf));
        target.setData(Codec.readBytes(buf));
    }

    @Override
    public B8900 newInstance() {
        return new B8900();
    }

}
