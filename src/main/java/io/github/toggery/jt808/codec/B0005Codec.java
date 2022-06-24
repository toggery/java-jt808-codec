package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0005;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Objects;

/**
 * JT/T 消息体编码解码器：0x0005 终端补传分包请求 // 2019 new
 *
 * @author togger
 */
public final class B0005Codec implements Codec<B0005> {

    private B0005Codec() {}

    /** 单例 */
    public static final B0005Codec INSTANCE = new B0005Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0005 target) {
        Codec.writeWord(buf, target.getOriginalSn());

        final List<Integer> bodyPacketSns = target.getBodyPacketSns();
        bodyPacketSns.removeIf(Objects::isNull);
        if (version > 0) {
            Codec.writeWord(buf, bodyPacketSns.size());
        } else {
            Codec.writeByte(buf, bodyPacketSns.size());
        }
        bodyPacketSns.stream().sorted().forEach(sn -> Codec.writeWord(buf, sn));
    }

    @Override
    public void decode(int version, ByteBuf buf, B0005 target) {
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
    public B0005 newInstance() {
        return new B0005();
    }

}
