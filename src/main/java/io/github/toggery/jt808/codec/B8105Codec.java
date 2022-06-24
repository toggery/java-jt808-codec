package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8105;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8105 终端控制
 *
 * @author togger
 */
public final class B8105Codec implements Codec<B8105> {

    private B8105Codec() {}

    /** 单例 */
    public static final B8105Codec INSTANCE = new B8105Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8105 target) {
        Codec.writeByte(buf, target.getCommand());
        Codec.writeString(buf, target.getParam());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8105 target) {
        target.setCommand(Codec.readByte(buf));
        target.setParam(Codec.readString(buf));
    }

    @Override
    public B8105 newInstance() {
        return new B8105();
    }

}
