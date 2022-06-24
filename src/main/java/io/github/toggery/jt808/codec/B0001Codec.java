package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0001;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0001 终端通用应答
 *
 * @author togger
 */
public final class B0001Codec implements Codec<B0001> {

    private B0001Codec() {}

    /** 单例 */
    public static final B0001Codec INSTANCE = new B0001Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0001 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeWord(buf, target.getReplyId());
        Codec.writeByte(buf, target.getResult());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0001 target) {
        target.setReplySn(Codec.readWord(buf));
        target.setReplyId(Codec.readWord(buf));
        target.setResult(Codec.readByte(buf));
    }

    @Override
    public B0001 newInstance() {
        return new B0001();
    }

}
