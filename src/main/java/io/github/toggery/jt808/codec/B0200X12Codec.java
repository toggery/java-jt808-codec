package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0200X12;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0200 位置附加信息】编码解码器：0x12 进出区域/路线报警附加信息，见表 29
 *
 * @author togger
 */
public final class B0200X12Codec implements Codec<B0200X12> {

    private B0200X12Codec() {}

    /** 单例 */
    public static final B0200X12Codec INSTANCE = new B0200X12Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0200X12 target) {
        Codec.writeByte(buf, target.getType());
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeByte(buf, target.getDirection());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0200X12 target) {
        target.setType(Codec.readByte(buf));
        target.setId(Codec.readDoubleWord(buf));
        target.setDirection(Codec.readByte(buf));
    }

    @Override
    public B0200X12 newInstance() {
        return new B0200X12();
    }

}
