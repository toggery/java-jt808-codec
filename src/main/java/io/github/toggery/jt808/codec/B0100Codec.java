package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0100;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0100 终端注册 // 2019 modify
 *
 * @author togger
 */
public final class B0100Codec implements Codec<B0100> {

    private B0100Codec() {}

    /** 单例 */
    public static final B0100Codec INSTANCE = new B0100Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0100 target) {
        Codec.writeWord(buf, target.getProvince());
        Codec.writeWord(buf, target.getCity());

        if (version > 0) {
            // 2019 前补 0x00（右对齐）
            Codec.writeChars(buf, target.getMaker(), 11, PadChar.NUL);
            Codec.writeChars(buf, target.getModel(), 30, PadChar.NUL);
            Codec.writeChars(buf, target.getId(), 30, PadChar.NUL);
        } else {
            // 2013 后补 0x00（左对齐）
            Codec.writeChars(buf, target.getMaker(), -5, PadChar.NUL);
            Codec.writeChars(buf, target.getModel(), -20, PadChar.NUL);
            Codec.writeChars(buf, target.getId(), -7, PadChar.NUL);
        }

        Codec.writeByte(buf, target.getPlateColor());
        Codec.writeString(buf, target.getPlateNo());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0100 target) {
        target.setProvince(Codec.readWord(buf));
        target.setCity(Codec.readWord(buf));

        if (version > 0) {
            // 2019
            target.setMaker(Codec.readChars(buf, 11));
            target.setModel(Codec.readChars(buf, 30));
            target.setId(Codec.readChars(buf, 30));
        } else {
            // 2013
            target.setMaker(Codec.readChars(buf, 5));
            // 兼容 2011
            target.setModel(Codec.readChars(buf, buf.readableBytes() > 27 ? 20 : 8));
            target.setId(Codec.readChars(buf, 7));
        }

        target.setPlateColor(Codec.readByte(buf));
        target.setPlateNo(Codec.readString(buf));
    }

    @Override
    public B0100 newInstance() {
        return new B0100();
    }

}
