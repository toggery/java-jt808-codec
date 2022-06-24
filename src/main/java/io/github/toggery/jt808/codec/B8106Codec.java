package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8106;
import io.netty.buffer.ByteBuf;

/**
 * JT/T 消息体编码解码器：0x8106 查询指定终端参数 元素类型为 DWORD
 *
 * @author togger
 */
public final class B8106Codec implements Codec<B8106> {

    private B8106Codec() {}

    /** 单例 */
    public static final B8106Codec INSTANCE = new B8106Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8106 target) {
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
    public void decode(int version, ByteBuf buf, B8106 target) {
        target.clear();

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            target.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B8106 newInstance() {
        return new B8106();
    }

}
