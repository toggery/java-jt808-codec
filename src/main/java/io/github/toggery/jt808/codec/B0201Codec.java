package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0201;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0201 位置信息查询应答】编码解码器
 *
 * @author togger
 */
public final class B0201Codec implements Codec<B0201> {

    private B0201Codec() {}

    /** 单例 */
    public static final B0201Codec INSTANCE = new B0201Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0201 target) {
        Codec.writeWord(buf, target.getReplySn());
        B0200Codec.INSTANCE.encode(version, buf, target);
    }

    @Override
    public void decode(int version, ByteBuf buf, B0201 target) {
        target.setReplySn(Codec.readWord(buf));
        B0200Codec.INSTANCE.decode(version, buf, target);
    }

    @Override
    public B0201 newInstance() {
        return new B0201();
    }

}
