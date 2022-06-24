package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0A00;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0A00 终端 RSA 公钥
 *
 * @author togger
 */
public final class B0A00Codec implements Codec<B0A00> {

    private B0A00Codec() {}

    /** 单例 */
    public static final B0A00Codec INSTANCE = new B0A00Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0A00 target) {
        Codec.writeDoubleWord(buf, target.getE());
        Codec.writeBytes(buf, target.getN());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0A00 target) {
        target.setE(Codec.readDoubleWord(buf));
        target.setN(Codec.readBytes(buf));
    }

    @Override
    public B0A00 newInstance() {
        return new B0A00();
    }

}
