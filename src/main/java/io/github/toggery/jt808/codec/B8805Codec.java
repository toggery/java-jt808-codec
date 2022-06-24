package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8805;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8805 单条存储多媒体数据检索上传命令
 *
 * @author togger
 */
public final class B8805Codec implements Codec<B8805> {

    private B8805Codec() {}

    /** 单例 */
    public static final B8805Codec INSTANCE = new B8805Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8805 target) {
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeByte(buf, target.getDeleted());
    }

    @Override
    public void decode(int version, ByteBuf buf, B8805 target) {
        target.setId(Codec.readDoubleWord(buf));
        target.setDeleted(Codec.readByte(buf));
    }

    @Override
    public B8805 newInstance() {
        return new B8805();
    }

}
