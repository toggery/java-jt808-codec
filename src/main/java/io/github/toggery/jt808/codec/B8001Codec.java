package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8001;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8001 平台通用应答
 *
 * @author togger
 */
public final class B8001Codec implements Codec<B8001> {

    private B8001Codec() {}

    /** 单例 */
    public static final B8001Codec INSTANCE = new B8001Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8001 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeWord(buf, target.getReplyId());
        Codec.writeByte(buf, target.getResult());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8001 target) {
        target.setReplySn(Codec.readWord(buf));
        target.setReplyId(Codec.readWord(buf));
        target.setResult(Codec.readByte(buf));
    }

    @Override
    public B8001 newInstance() {
        return new B8001();
    }

}
