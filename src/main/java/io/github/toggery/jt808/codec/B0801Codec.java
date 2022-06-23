package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0801;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0801 多媒体数据上传（位置信息汇报的信息基本数据）】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B0801Codec implements Codec<B0801> {

    private B0801Codec() {}

    /** 单例 */
    public static final B0801Codec INSTANCE = new B0801Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0801 target) {
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeByte(buf, target.getType());
        Codec.writeByte(buf, target.getFormat());
        Codec.writeByte(buf, target.getEvent());
        Codec.writeByte(buf, target.getChannel());

        B0200Codec.INSTANCE.encodeBase(version, buf, target);

        Codec.writeBytes(buf, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0801 target) {
        target.setId(Codec.readDoubleWord(buf));
        target.setType(Codec.readByte(buf));
        target.setFormat(Codec.readByte(buf));
        target.setEvent(Codec.readByte(buf));
        target.setChannel(Codec.readByte(buf));

        B0200Codec.INSTANCE.decodeBase(version, buf, target);

        target.setData(Codec.readBytes(buf));
    }

    @Override
    public B0801 newInstance() {
        return new B0801();
    }

}
