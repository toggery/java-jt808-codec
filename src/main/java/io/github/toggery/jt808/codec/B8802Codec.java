package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8802;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8802 存储多媒体数据检索】编码解码器
 *
 * <p>注：不按时间范围则将起始时间/结束时间都设为000000000000。</p>
 *
 * @author togger
 */
public final class B8802Codec implements Codec<B8802> {

    private B8802Codec() {}

    /** 单例 */
    public static final B8802Codec INSTANCE = new B8802Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8802 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeByte(buf, target.getChannel());
        Codec.writeByte(buf, target.getEvent());
        Codec.writeBcd(buf, target.getStartTime(), 6);
        Codec.writeBcd(buf, target.getEndTime(), 6);
    }

    @Override
    public void decode(int version, ByteBuf buf, B8802 target) {
        target.setType(Codec.readByte(buf));
        target.setChannel(Codec.readByte(buf));
        target.setEvent(Codec.readByte(buf));
        target.setStartTime(Codec.readBcd(buf, 6, false));
        target.setEndTime(Codec.readBcd(buf, 6, false));
    }

    @Override
    public B8802 newInstance() {
        return new B8802();
    }

}
