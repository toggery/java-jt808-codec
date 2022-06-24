package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0302;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0302 提问应答 // 2019 del
 *
 * @author togger
 */
public final class B0302Codec implements Codec<B0302> {

    private B0302Codec() {}

    /** 单例 */
    public static final B0302Codec INSTANCE = new B0302Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0302 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeByte(buf, target.getId());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0302 target) {
        target.setReplySn(Codec.readWord(buf));
        target.setId(Codec.readByte(buf));
    }

    @Override
    public B0302 newInstance() {
        return new B0302();
    }

}
