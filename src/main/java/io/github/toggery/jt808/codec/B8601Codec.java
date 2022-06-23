package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8601;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8601 删除圆形区域】编码解码器 元素类型为 DWORD
 *
 * <p>本条消息中包含的区域数，不超过 125 个，多于 125 个建议用多条消息，0 为删除所有圆形区域</p>
 *
 * @author togger
 */
public final class B8601Codec implements Codec<B8601> {

    private B8601Codec() {}

    /** 单例 */
    public static final B8601Codec INSTANCE = new B8601Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8601 target) {
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
    public void decode(int version, ByteBuf buf, B8601 target) {
        target.clear();

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            target.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B8601 newInstance() {
        return new B8601();
    }

}
