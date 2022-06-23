package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0705;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Objects;

/**
 * JT/T 消息体【0x0705 CAN 总线数据上传】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B0705Codec implements Codec<B0705> {

    private B0705Codec() {}

    /** 单例 */
    public static final B0705Codec INSTANCE = new B0705Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0705 target) {
        final List<B0705.Can> cans = target.getCans();
        cans.removeIf(Objects::isNull);

        Codec.writeWord(buf, cans.size());
        Codec.writeBcd(buf, target.getTime(), 5);
        cans.forEach(i -> CanCodec.INSTANCE.encode(version, buf, i));
    }

    @Override
    public void decode(int version, ByteBuf buf, B0705 target) {
        final List<B0705.Can> cans = target.getCans();
        cans.clear();

        int cnt = Codec.readWord(buf);
        target.setTime(Codec.readBcd(buf, 5, false));
        while (cnt-- > 0) {
            cans.add(CanCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B0705 newInstance() {
        return new B0705();
    }


    /**
     * JT/T 消息体【0x0705 CAN 总线数据】编码解码器 // 2019 modify
     *
     * @author togger
     */
    public static final class CanCodec implements Codec<B0705.Can> {

        private CanCodec() {}

        /** 单例  */
        public static final CanCodec INSTANCE = new CanCodec();

        @Override
        public void encode(int version, ByteBuf buf, B0705.Can target) {
            Codec.writeDoubleWord(buf, target.getId());
            Codec.writeBytes(buf, target.getData(), -8, PadChar.NUL);
        }

        @Override
        public void decode(int version, ByteBuf buf, B0705.Can target) {
            target.setId(Codec.readDoubleWord(buf));
            target.setData(Codec.readBytes(buf, 8, PadChar.NUL));
        }

        @Override
        public B0705.Can newInstance() {
            return new B0705.Can();
        }

    }

}
