package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8300;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8300 文本信息下发】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B8300Codec implements Codec<B8300> {

    private B8300Codec() {}

    /** 单例 */
    public static final B8300Codec INSTANCE = new B8300Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8300 target) {
        Codec.writeByte(buf, target.getProps());

        if (version > 0) {
            Codec.writeByte(buf, target.getType());
        }

        Codec.writeString(buf, target.getContent());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8300 target) {
        target.setProps(Codec.readByte(buf));

        target.setType(version > 0 ? Codec.readByte(buf) : 0);

        target.setContent(Codec.readString(buf));
    }

    @Override
    public B8300 newInstance() {
        return new B8300();
    }

}
