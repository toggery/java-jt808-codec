package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8608;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体【0x8608 查询区域或线路数据】编码解码器 元素类型为 DWORD // 2019 new
 *
 * @author togger
 */
public final class B8608Codec implements Codec<B8608> {

    private B8608Codec() {}

    /** 单例 */
    public static final B8608Codec INSTANCE = new B8608Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8608 target) {
        Codec.writeByte(buf, target.getType());

        Codec.writeCountHeadedContent(buf, IntUnit.DWORD, target.getIds(), (b, that) -> {
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
    public void decode(int version, ByteBuf buf, B8608 target) {
        final List<Long> ids = target.getIds();
        ids.clear();
        target.setType(Codec.readByte(buf));

        long cnt = Codec.readDoubleWord(buf);
        while (cnt-- > 0) {
            ids.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B8608 newInstance() {
        return new B8608();
    }

}
