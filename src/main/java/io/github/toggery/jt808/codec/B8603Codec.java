package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8603;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体【0x8603 删除矩形区域】编码解码器 元素类型为 DWORD
 *
 * <p>本条消息中包含的区域数，不超过 125 个，多于 125 个建议用多条消息，0 为删除所有矩形区域</p>
 *
 * @author togger
 */
public final class B8603Codec implements Codec<B8603> {

    private B8603Codec() {}

    /** 单例 */
    public static final B8603Codec INSTANCE = new B8603Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8603 target) {
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
    public void decode(int version, ByteBuf buf, B8603 target) {
        target.clear();

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            target.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B8603 newInstance() {
        return new B8603();
    }

}
