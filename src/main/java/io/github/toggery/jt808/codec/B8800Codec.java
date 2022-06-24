package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8800;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Objects;

/**
 * JT/T 消息体编码解码器：0x8800 多媒体数据上传应答
 *
 * @author togger
 */
public final class B8800Codec implements Codec<B8800> {

    private B8800Codec() {}

    /** 单例 */
    public static final B8800Codec INSTANCE = new B8800Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8800 target) {
        Codec.writeDoubleWord(buf, target.getId());
        final List<Integer> bodyPacketSns = target.getBodyPacketSns();
        bodyPacketSns.removeIf(Objects::isNull);
        if (bodyPacketSns.size() <= 0) {
            return;
        }

        Codec.writeByte(buf, bodyPacketSns.size());
        bodyPacketSns.forEach(i -> Codec.writeWord(buf, i));
    }

    @Override
    public void decode(int version, ByteBuf buf, B8800 target) {
        final List<Integer> bodyPacketSns = target.getBodyPacketSns();
        bodyPacketSns.clear();
        target.setId(Codec.readDoubleWord(buf));

        if (buf.isReadable()) {
            int cnt = Codec.readByte(buf);
            while (cnt-- > 0) {
                bodyPacketSns.add(Codec.readWord(buf));
            }
        }
    }

    @Override
    public B8800 newInstance() {
        return new B8800();
    }

}
