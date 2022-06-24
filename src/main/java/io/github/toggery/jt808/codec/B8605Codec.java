package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8605;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8605 删除多边形区域 元素类型为 DWORD
 *
 * <p>本条消息中包含的区域数，不超过 125 个，多于 125 个建议用多条消息，0 为删除所有多边形区域</p>
 * @author togger
 */
public final class B8605Codec implements Codec<B8605> {

    private B8605Codec() {}

    /** 单例 */
    public static final B8605Codec INSTANCE = new B8605Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8605 target) {
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, target, (b, that) -> {
            int count = 0;
            for (final Long id : that) {
                if (id == null) continue;
                Codec.writeDoubleWord(buf, id);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B8605 target) {
        target.clear();

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            target.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B8605 newInstance() {
        return new B8605();
    }

}
