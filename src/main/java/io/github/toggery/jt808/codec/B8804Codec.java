package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8804;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8804 录音开始命令】编码解码器
 *
 * @author togger
 */
public final class B8804Codec implements Codec<B8804> {

    private B8804Codec() {}

    /** 单例 */
    public static final B8804Codec INSTANCE = new B8804Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8804 target) {
        Codec.writeByte(buf, target.getCommand());
        Codec.writeWord(buf, target.getDuration());
        Codec.writeByte(buf, target.getAction());
        Codec.writeByte(buf, target.getSampling());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8804 target) {
        target.setCommand(Codec.readByte(buf));
        target.setDuration(Codec.readWord(buf));
        target.setAction(Codec.readByte(buf));
        target.setSampling(Codec.readByte(buf));
    }

    @Override
    public B8804 newInstance() {
        return new B8804();
    }

}
