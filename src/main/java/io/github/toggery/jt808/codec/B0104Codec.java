package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0104;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0104 查询终端参数应答
 *
 * @author togger
 */
public final class B0104Codec implements Codec<B0104> {

    private B0104Codec() {}

    /** 单例 */
    public static final B0104Codec INSTANCE = new B0104Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0104 target) {
        Codec.writeWord(buf, target.getReplySn());

        B8103Codec.INSTANCE.encode(version, buf, target);
    }

    @Override
    public void decode(int version, ByteBuf buf, B0104 target) {
        target.setReplySn(Codec.readWord(buf));

        B8103Codec.INSTANCE.decode(version, buf, target);
    }

    @Override
    public B0104 newInstance() {
        return new B0104();
    }

}
