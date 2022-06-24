package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8003;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Objects;

/**
 * JT/T 消息体编码解码器：0x8003 服务器补传分包请求
 *
 * @author togger
 */
public final class B8003Codec implements Codec<B8003> {

    private B8003Codec() {}

    /** 单例 */
    public static final B8003Codec INSTANCE = new B8003Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8003 target) {
        Codec.writeWord(buf, target.getOriginalSn());

        final List<Integer> bodyPacketSns = target.getBodyPacketSns();
        bodyPacketSns.removeIf(Objects::isNull);
        if (version > 0) {
            Codec.writeWord(buf, bodyPacketSns.size());
        } else {
            Codec.writeByte(buf, bodyPacketSns.size());
        }
        bodyPacketSns.forEach(sn -> Codec.writeWord(buf, sn));
    }

    @Override
    public void decode(int version, ByteBuf buf, B8003 target) {
        target.setOriginalSn(Codec.readWord(buf));

        final List<Integer> bodyPacketSns = target.getBodyPacketSns();
        bodyPacketSns.clear();
        int cnt;
        if (version > 0) {
            cnt = Codec.readWord(buf);
        } else {
            cnt = Codec.readByte(buf);
        }
        while (cnt-- > 0) {
            bodyPacketSns.add(Codec.readWord(buf));
        }
    }

    @Override
    public B8003 newInstance() {
        return new B8003();
    }

}
