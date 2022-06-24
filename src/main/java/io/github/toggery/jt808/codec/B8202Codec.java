package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8202;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8202 临时位置跟踪控制
 *
 * @author togger
 */
public final class B8202Codec implements Codec<B8202> {

    private B8202Codec() {}

    /** 单例 */
    public static final B8202Codec INSTANCE = new B8202Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8202 target) {
        Codec.writeWord(buf, target.getInterval());
        if (target.getInterval() > 0) {
            Codec.writeDoubleWord(buf, target.getDuration());
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B8202 target) {
        target.setInterval(Codec.readWord(buf));
        target.setDuration(target.getInterval() > 0 ? Codec.readDoubleWord(buf) : 0);
    }

    @Override
    public B8202 newInstance() {
        return new B8202();
    }

}
