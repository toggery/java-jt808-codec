package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8108;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8108 下发终端升级包】编码解码器
 *
 * @author togger
 */
public final class B8108Codec implements Codec<B8108> {

    private B8108Codec() {}

    /** 单例 */
    public static final B8108Codec INSTANCE = new B8108Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8108 target) {
        Codec.writeByte(buf, target.getType());
        // 后补 0x00（左对齐）
        Codec.writeChars(buf, target.getMaker(), -5, PadChar.NUL);
        Codec.writeString(buf, IntUnit.BYTE, target.getVersion());
        Codec.writeBytes(buf, IntUnit.DWORD, target.getData());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8108 target) {
        target.setType(Codec.readByte(buf));
        target.setMaker(Codec.readChars(buf, 5));
        target.setVersion(Codec.readString(buf, IntUnit.BYTE));
        target.setData(Codec.readBytes(buf, IntUnit.DWORD));
    }

    @Override
    public B8108 newInstance() {
        return new B8108();
    }

}
