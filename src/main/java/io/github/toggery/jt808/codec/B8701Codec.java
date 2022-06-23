package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8701;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8701 行驶记录参数下传命令】编码解码器
 *
 * @author togger
 */
public final class B8701Codec implements Codec<B8701> {

    private B8701Codec() {}

    /** 单例 */
    public static final B8701Codec INSTANCE = new B8701Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8701 target) {
        Codec.writeByte(buf, target.getCommand());
        Codec.writeBytes(buf, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8701 target) {
        target.setCommand(Codec.readByte(buf));
        target.setData(Codec.readBytes(buf));
    }

    @Override
    public B8701 newInstance() {
        return new B8701();
    }

}
