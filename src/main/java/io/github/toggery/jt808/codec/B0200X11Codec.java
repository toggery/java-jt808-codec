package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0200X11;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x0200 位置信息汇报 附加信息 0x11 超速报警附加信息，见表 28
 *
 * @author togger
 */
public final class B0200X11Codec implements Codec<B0200X11> {

    private B0200X11Codec() {}

    /** 单例 */
    public static final B0200X11Codec INSTANCE = new B0200X11Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0200X11 target) {
        Codec.writeByte(buf, target.getType());
        if (target.getType() != 0) {
            Codec.writeDoubleWord(buf, target.getId());
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B0200X11 target) {
        target.setType(Codec.readByte(buf));
        if (target.getType() != 0) {
            target.setId(Codec.readDoubleWord(buf));
        } else {
            target.setId(0L);
        }
    }

    @Override
    public B0200X11 newInstance() {
        return new B0200X11();
    }

}
