package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8604;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 *  JT/T 消息体编码解码器：0x8604 设置多边形区域 // 2019 modify
 *
 * @author togger
 */
public final class B8604Codec implements Codec<B8604> {

    private B8604Codec() {}

    /** 单例 */
    public static final B8604Codec INSTANCE = new B8604Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8604 target) {
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeWord(buf, target.getProps());

        if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
            Codec.writeBcd(buf, target.getStartTime(), 6);
            Codec.writeBcd(buf, target.getEndTime(), 6);
        }

        final boolean limitSpeed = (target.getProps() & BIT1_MASK) == BIT1_MASK;
        if (limitSpeed) {
            Codec.writeWord(buf, target.getMaxSpeed());
            Codec.writeByte(buf, target.getDuration());
        }

        Codec.writeCountHeadedContent(buf, IntUnit.WORD, target.getPoints(), (b, that) -> {
            int count = 0;
            for (final B8604.Point point : that) {
                if (point == null) continue;
                PointCodec.INSTANCE.encode(version, b, point);
                count++;
            }
            return count;
        });

        if (version > 0) {
            if (limitSpeed) {
                Codec.writeWord(buf, target.getNightMaxSpeed());
            }
            Codec.writeString(buf, IntUnit.WORD, target.getName());
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B8604 target) {
        final List<B8604.Point> points = target.getPoints();
        points.clear();
        target.setId(Codec.readDoubleWord(buf));
        target.setProps(Codec.readWord(buf));

        if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
            target.setStartTime(Codec.readBcd(buf,6, false));
            target.setEndTime(Codec.readBcd(buf,6, false));
        }

        final boolean limitSpeed = (target.getProps() & BIT1_MASK) == BIT1_MASK;
        if (limitSpeed) {
            target.setMaxSpeed(Codec.readWord(buf));
            target.setDuration(Codec.readByte(buf));
        }

        int cnt = Codec.readWord(buf);
        while (cnt-- > 0) {
            points.add(PointCodec.INSTANCE.decode(version, buf));
        }

        if (version > 0) {
            if (limitSpeed) {
                target.setNightMaxSpeed (Codec.readWord(buf));
            }
            target.setName(Codec.readString(buf, IntUnit.WORD));
        }
    }

    @Override
    public B8604 newInstance() {
        return new B8604();
    }


    /** {@link B8604#getProps()} 二进制位 0 掩码 */
    private static final int BIT0_MASK = 0b1;
    /** {@link B8604#getProps()} 二进制位 1 掩码 */
    private static final int BIT1_MASK = 0b10;


    /**
     * JT/T 消息体编码解码器：0x8604 多边形区域 // 2019 modify
     *
     * @author togger
     */
    public static final class PointCodec implements Codec<B8604.Point> {

        private PointCodec() {}

        /** 单例 */
        public static final PointCodec INSTANCE = new PointCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8604.Point target) {
            Codec.writeDoubleWord(buf, target.getLatitude());
            Codec.writeDoubleWord(buf, target.getLongitude());
        }

        @Override
        public void decode(int version, ByteBuf buf, B8604.Point target) {
            target.setLatitude(Codec.readDoubleWord(buf));
            target.setLongitude(Codec.readDoubleWord(buf));
        }

        @Override
        public B8604.Point newInstance() {
            return new B8604.Point();
        }

    }

}
