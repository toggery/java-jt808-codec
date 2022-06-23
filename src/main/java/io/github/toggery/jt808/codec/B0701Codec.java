package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0701;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0701 电子运单上报】编码解码器
 *
 * @author togger
 */
public final class B0701Codec implements Codec<B0701> {

    private B0701Codec() {}

    /** 单例 */
    public static final B0701Codec INSTANCE = new B0701Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0701 target) {
        Codec.writeBytes(buf, IntUnit.DWORD, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0701 target) {
        target.setData(Codec.readBytes(buf, IntUnit.DWORD));
    }

    @Override
    public B0701 newInstance() {
        return new B0701();
    }

}
