package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0700;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0700 行驶记录数据上传（应答）
 *
 * @author togger
 */
public final class B0700Codec implements Codec<B0700> {

    private B0700Codec() {}

    /** 单例 */
    public static final B0700Codec INSTANCE = new B0700Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0700 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeByte(buf, target.getCommand());
        Codec.writeBytes(buf, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0700 target) {
        target.setReplySn(Codec.readWord(buf));
        target.setCommand(Codec.readByte(buf));
        target.setData(Codec.readBytes(buf));
    }

    @Override
    public B0700 newInstance() {
        return new B0700();
    }

}
