package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0102;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x0102 终端鉴权】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B0102Codec implements Codec<B0102> {

    private B0102Codec() {}

    /** 单例 */
    public static final B0102Codec INSTANCE = new B0102Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0102 target) {
        if (version > 0) {
            Codec.writeString(buf, IntUnit.BYTE, target.getToken());
            // 后补 0x00 （左对齐）
            Codec.writeChars(buf, target.getImei(), -15, PadChar.NUL);
            Codec.writeChars(buf, target.getVersion(), -20, PadChar.NUL);
            return;
        }

        Codec.writeString(buf, target.getToken());
    }

    @Override
    public void decode(int version, ByteBuf buf, B0102 target) {
        if (version > 0) {
            target.setToken(Codec.readString(buf, IntUnit.BYTE));
            target.setImei(Codec.readChars(buf, 15));
            target.setVersion(Codec.readChars(buf, 20));
            return;
        }

        target.setToken(Codec.readString(buf));
        target.setImei(null);
        target.setVersion(null);
    }

    @Override
    public B0102 newInstance() {
        return new B0102();
    }

}
