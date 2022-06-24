package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8A00;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8A00 平台 RSA 公钥
 *
 * @author togger
 */
public final class B8A00Codec implements Codec<B8A00> {

    private B8A00Codec() {}

    /** 单例 */
    public static final B8A00Codec INSTANCE = new B8A00Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8A00 target) {
        Codec.writeDoubleWord(buf, target.getE());
        Codec.writeBytes(buf, target.getN());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8A00 target) {
        target.setE(Codec.readDoubleWord(buf));
        target.setN(Codec.readBytes(buf));
    }

    @Override
    public B8A00 newInstance() {
        return new B8A00();
    }

}