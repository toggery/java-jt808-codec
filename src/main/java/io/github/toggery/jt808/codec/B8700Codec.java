package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8700;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8700 行驶记录数据采集命令
 *
 * @author togger
 */
public final class B8700Codec implements Codec<B8700> {

    private B8700Codec() {}

    /** 单例 */
    public static final B8700Codec INSTANCE = new B8700Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8700 target) {
        Codec.writeByte(buf, target.getCommand());
        Codec.writeBytes(buf, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8700 target) {
        target.setCommand(Codec.readByte(buf));
        target.setData(Codec.readBytes(buf));
    }

    @Override
    public B8700 newInstance() {
        return new B8700();
    }

}
