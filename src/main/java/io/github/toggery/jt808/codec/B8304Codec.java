package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8304;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8304 信息服务】编码解码器 // 2019 del
 *
 * @author togger
 */
public final class B8304Codec implements Codec<B8304> {

    private B8304Codec() {}

    /** 单例 */
    public static final B8304Codec INSTANCE = new B8304Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8304 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeString(buf, IntUnit.WORD, target.getContent());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8304 target) {
        target.setType(Codec.readByte(buf));
        target.setContent(Codec.readString(buf, IntUnit.WORD));
    }

    @Override
    public B8304 newInstance() {
        return new B8304();
    }

}
