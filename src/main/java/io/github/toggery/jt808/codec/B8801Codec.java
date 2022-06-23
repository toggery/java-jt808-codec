package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8801;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8801 摄像头立即拍摄命令】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B8801Codec implements Codec<B8801> {

   private B8801Codec() {}

    /** 单例 */
    public static final B8801Codec INSTANCE = new B8801Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8801 target) {
        Codec.writeByte(buf, target.getChannel());
        Codec.writeWord(buf, target.getCommand());
        Codec.writeWord(buf, target.getInterval());
        Codec.writeByte(buf, target.getAction());
        Codec.writeByte(buf, target.getResolution());
        Codec.writeByte(buf, target.getQuality());
        Codec.writeByte(buf, target.getBrightness());
        Codec.writeByte(buf, target.getContrast());
        Codec.writeByte(buf, target.getSaturation());
        Codec.writeByte(buf, target.getChroma());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8801 target) {
        target.setChannel(Codec.readByte(buf));
        target.setCommand(Codec.readWord(buf));
        target.setInterval(Codec.readWord(buf));
        target.setAction(Codec.readByte(buf));
        target.setResolution(Codec.readByte(buf));
        target.setQuality(Codec.readByte(buf));
        target.setBrightness(Codec.readByte(buf));
        target.setContrast(Codec.readByte(buf));
        target.setSaturation(Codec.readByte(buf));
        target.setChroma(Codec.readByte(buf));
    }

    @Override
    public B8801 newInstance() {
        return new B8801();
    }

}
