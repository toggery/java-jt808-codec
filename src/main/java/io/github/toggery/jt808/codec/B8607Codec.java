package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8607;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8607 删除路线 元素类型为 DWORD
 *
 * <p>本条消息中包含的路线数，不超过 125 个，多于 125 个建议用多条消息，0 为删除所有路线</p>
 * @author togger
 */
public final class B8607Codec implements Codec<B8607> {

    private B8607Codec() {}

    /** 单例 */
    public static final B8607Codec INSTANCE = new B8607Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8607 target) {
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
    public void decode(int version, ByteBuf buf, B8607 target) {
        target.clear();

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            target.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B8607 newInstance() {
        return new B8607();
    }

}
