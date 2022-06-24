package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0800;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0800 多媒体事件信息上传
 *
 * @author togger
 */
public final class B0800Codec implements Codec<B0800> {

    private B0800Codec() {}

    /** 单例 */
    public static final B0800Codec INSTANCE = new B0800Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0800 target) {
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeByte(buf, target.getType());
        Codec.writeByte(buf, target.getFormat());
        Codec.writeByte(buf, target.getEvent());
        Codec.writeByte(buf, target.getChannel());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0800 target) {
        target.setId(Codec.readDoubleWord(buf));
        target.setType(Codec.readByte(buf));
        target.setFormat(Codec.readByte(buf));
        target.setEvent(Codec.readByte(buf));
        target.setChannel(Codec.readByte(buf));
    }

    @Override
    public B0800 newInstance() {
        return new B0800();
    }

}
