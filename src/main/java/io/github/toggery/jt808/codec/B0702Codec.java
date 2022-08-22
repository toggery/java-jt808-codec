package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0702;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0702 驾驶员身份信息采集上报 // 2019 modify
 *
 * @author togger
 */
public final class B0702Codec implements Codec<B0702> {

    private B0702Codec() {}

    /** 单例 */
    public static final B0702Codec INSTANCE = new B0702Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0702 target) {
        Codec.writeByte(buf, target.getStatus());
        Codec.writeBcd(buf, target.getTime(), 6);
        if (target.getStatus() != B0702.STATUS_IC_CARD_INSERTED) {
            return;
        }

        Codec.writeByte(buf, target.getResult());
        if (target.getResult() != B0702.RESULT_SUCCESSFUL) {
            return;
        }

        Codec.writeString(buf, IntUnit.BYTE, target.getName());
        // 后补 0x00（左对齐）
        Codec.writeChars(buf, target.getLicenseNo(), -20, PadChar.NUL);
        Codec.writeString(buf, IntUnit.BYTE, target.getAuthority());
        Codec.writeBcd(buf, target.getValidity(), 4);

        if (version > 0) {
            // 后补 0x00（左对齐）
            Codec.writeChars(buf, target.getIdCardNo(), -20, PadChar.NUL);
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B0702 target) {
        target.setResult(0);
        target.setName(null);
        target.setLicenseNo(null);
        target.setAuthority(null);
        target.setValidity(null);
        target.setIdCardNo(null);

        target.setStatus(Codec.readByte(buf));
        target.setTime(Codec.readBcd(buf, 6, false));
        if (target.getStatus() != B0702.STATUS_IC_CARD_INSERTED) {
            return;
        }

        target.setResult(Codec.readByte(buf));
        if (target.getResult() != B0702.RESULT_SUCCESSFUL) {
            return;
        }

        target.setName(Codec.readString(buf, IntUnit.BYTE));
        target.setLicenseNo(Codec.readChars(buf, 20));
        target.setAuthority(Codec.readString(buf, IntUnit.BYTE));
        target.setValidity(Codec.readBcd(buf, 4, false));

        if (version > 0) {
            target.setIdCardNo(Codec.readChars(buf, 20));
        }
    }

    @Override
    public B0702 newInstance() {
        return new B0702();
    }

}
