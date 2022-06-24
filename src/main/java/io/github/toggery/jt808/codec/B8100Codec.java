package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8100;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8100 终端注册应答
 *
 * @author togger
 */
public final class B8100Codec implements Codec<B8100> {

    private B8100Codec() {}

    /** 单例 */
    public static final B8100Codec INSTANCE = new B8100Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8100 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeByte(buf, target.getResult());
        if (target.getResult() == 0) {
            Codec.writeString(buf, target.getToken());
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B8100 target) {
        target.setReplySn(Codec.readWord(buf));
        target.setResult(Codec.readByte(buf));
        target.setToken(target.getResult() == 0 ? Codec.readString(buf) : null);
    }

    @Override
    public B8100 newInstance() {
        return new B8100();
    }

}
