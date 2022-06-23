package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0500;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0500 车辆控制应答】编码解码器
 *
 * @author togger
 */
public final class B0500Codec implements Codec<B0500> {

    private B0500Codec() {}

    /** 单例 */
    public static final B0500Codec INSTANCE = new B0500Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0500 target) {
        Codec.writeWord(buf, target.getReplySn());
        B0200Codec.INSTANCE.encode(version, buf, target);
    }

    @Override
    public void decode(int version, ByteBuf buf, B0500 target) {
        target.setReplySn(Codec.readWord(buf));
        B0200Codec.INSTANCE.decode(version, buf, target);
    }

    @Override
    public B0500 newInstance() {
        return new B0500();
    }

}
