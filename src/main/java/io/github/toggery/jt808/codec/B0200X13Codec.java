package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0200X13;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0200 位置信息汇报 附加信息 0x13 路段行驶时间不足/过长报警附加信息，见表 30
 *
 * @author togger
 */
public final class B0200X13Codec implements Codec<B0200X13> {

    private B0200X13Codec() {}

    /** 单例 */
    public static final B0200X13Codec INSTANCE = new B0200X13Codec();


    @Override
    public void encode(int version, ByteBuf buf, B0200X13 target) {
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeWord(buf, target.getDuration());
        Codec.writeByte(buf, target.getResult());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0200X13 target) {
        target.setId(Codec.readDoubleWord(buf));
        target.setDuration(Codec.readWord(buf));
        target.setResult(Codec.readByte(buf));
    }

    @Override
    public B0200X13 newInstance() {
        return new B0200X13();
    }

}
