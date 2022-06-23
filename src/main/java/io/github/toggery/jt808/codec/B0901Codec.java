package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0901;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0901 数据压缩上报】编码解码器
 *
 * @author togger
 */
public final class B0901Codec implements Codec<B0901> {

    private B0901Codec() {}

    /** 单例 */
    public static final B0901Codec INSTANCE = new B0901Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0901 target) {
        Codec.writeBytes(buf, IntUnit.DWORD, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0901 target) {
        target.setData(Codec.readBytes(buf, IntUnit.DWORD));
    }

    @Override
    public B0901 newInstance() {
        return new B0901();
    }

}
