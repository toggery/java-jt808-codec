package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0200;
import io.github.toggery.jt808.messagebody.B0704;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Objects;

/**
 * JT/T 消息体【0x0704 定位数据批量上传】编码解码器
 *
 * @author togger
 */
public final class B0704Codec implements Codec<B0704> {

    private B0704Codec() {}

    /** 单例 */
    public static final B0704Codec INSTANCE = new B0704Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0704 target) {
        final List<B0200> locations = target.getLocations();
        locations.removeIf(Objects::isNull);

        Codec.writeWord(buf, locations.size());
        Codec.writeByte(buf, target.getType());
        locations.forEach(i -> Codec.writeLengthHeadedContent(buf, IntUnit.WORD, i
                , (b, v) -> B0200Codec.INSTANCE.encode(version, b, v)));
    }

    @Override
    public void decode(int version, ByteBuf buf, B0704 target) {
        final List<B0200> locations = target.getLocations();
        locations.clear();

        int cnt = Codec.readWord(buf);
        target.setType(Codec.readByte(buf));
        while (cnt-- > 0) {
            final ByteBuf locationBuf = Codec.readSlice(buf, IntUnit.WORD);
            locations.add(B0200Codec.INSTANCE.decode(version, locationBuf));
        }
    }

    @Override
    public B0704 newInstance() {
        return new B0704();
    }

}
