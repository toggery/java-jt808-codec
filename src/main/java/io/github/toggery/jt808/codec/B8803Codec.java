package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8803;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8803 存储多媒体数据上传
 *
 * @author togger
 */
public final class B8803Codec implements Codec<B8803> {

    private B8803Codec() {}

    /** 单例 */
    public static final B8803Codec INSTANCE = new B8803Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8803 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeByte(buf, target.getChannel());
        Codec.writeByte(buf, target.getEvent());
        Codec.writeBcd(buf, target.getStartTime(), 6);
        Codec.writeBcd(buf, target.getEndTime(), 6);
        Codec.writeByte(buf, target.getDeleted());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8803 target) {
        target.setType(Codec.readByte(buf));
        target.setChannel(Codec.readByte(buf));
        target.setEvent(Codec.readByte(buf));
        target.setStartTime(Codec.readBcd(buf, 6, false));
        target.setEndTime(Codec.readBcd(buf, 6, false));
        target.setDeleted(Codec.readByte(buf));
    }

    @Override
    public B8803 newInstance() {
        return new B8803();
    }

}
