package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8203;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8203 人工确认报警消息】编码解码器
 *
 * @author togger
 */
public final class B8203Codec implements Codec<B8203> {

    private B8203Codec() {}

    /** 单例 */
    public static final B8203Codec INSTANCE = new B8203Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8203 target) {
        Codec.writeWord(buf, target.getOriginalSn());
        Codec.writeDoubleWord(buf, target.getTypeBits());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8203 target) {
        target.setOriginalSn(Codec.readWord(buf));
        target.setTypeBits(Codec.readDoubleWord(buf));
    }

    @Override
    public B8203 newInstance() {
        return new B8203();
    }

}
