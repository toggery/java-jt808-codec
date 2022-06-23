package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0107;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0107 查询终端属性应答】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B0107Codec implements Codec<B0107> {

    private B0107Codec() {}

    /** 单例 */
    public static final B0107Codec INSTANCE = new B0107Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0107 target) {
        Codec.writeWord(buf, target.getType());
        // 后补 0x00 （左对齐）
        Codec.writeChars(buf, target.getMaker(), -5, PadChar.NUL);
        Codec.writeChars(buf, target.getModel(), version > 0 ? -30 : -20, PadChar.NUL);
        Codec.writeChars(buf, target.getId(), version > 0 ? -30 : -7, PadChar.NUL);
        Codec.writeBcd(buf, target.getSimId(), 10);
        Codec.writeString(buf, IntUnit.BYTE, target.getHw());
        Codec.writeString(buf, IntUnit.BYTE, target.getFm());
        Codec.writeByte(buf, target.getGnss());
        Codec.writeByte(buf, target.getComm());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0107 target) {
        target.setType(Codec.readWord(buf));
        target.setMaker(Codec.readChars(buf, 5));
        target.setModel(Codec.readChars(buf, version > 0 ? 30 : 20));
        target.setId(Codec.readChars(buf, version > 0 ? 30 : 7));
        target.setSimId(Codec.readBcd(buf, 10, true));
        target.setHw(Codec.readString(buf, IntUnit.BYTE));
        target.setFm(Codec.readString(buf, IntUnit.BYTE));
        target.setGnss(Codec.readByte(buf));
        target.setComm(Codec.readByte(buf));
    }

    @Override
    public B0107 newInstance() {
        return new B0107();
    }

}
