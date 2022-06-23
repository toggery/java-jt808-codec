package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8606;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体【0x8606 设置路线】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B8606Codec implements Codec<B8606> {

    private B8606Codec() {}

    /** 单例 */
    public static final B8606Codec INSTANCE = new B8606Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8606 target) {
        Codec.writeDoubleWord(buf, target.getId());
        Codec.writeWord(buf, target.getProps());

        if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
            Codec.writeBcd(buf, target.getStartTime(), 6);
            Codec.writeBcd(buf, target.getEndTime(), 6);
        }

        Codec.writeCountHeadedContent(buf, IntUnit.WORD, target.getPoints(), (b, that) -> {
            int count = 0;
            for (final B8606.Point point : that) {
                if (point == null) continue;
                PointCodec.INSTANCE.encode(version, b, point);
                count++;
            }
            return count;
        });

        if (version > 0) {
            Codec.writeString(buf, IntUnit.WORD, target.getName());
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B8606 target) {
        final List<B8606.Point> points = target.getPoints();
        points.clear();
        target.setId(Codec.readDoubleWord(buf));
        target.setProps(Codec.readWord(buf));

        if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
            target.setStartTime(Codec.readBcd(buf, 6, false));
            target.setEndTime(Codec.readBcd(buf, 6, false));
        }

        int cnt = Codec.readWord(buf);
        while (cnt-- > 0) {
            points.add(PointCodec.INSTANCE.decode(version, buf));
        }

        if (version > 0) {
            target.setName(Codec.readString(buf, IntUnit.WORD));
        }
    }

    @Override
    public B8606 newInstance() {
        return new B8606();
    }


    /** {@link B8606.Point#getProps()} 二进制位 0 掩码 */
    private static final int BIT0_MASK = 0b1;
    /** {@link B8606.Point#getProps()} 二进制位 1 掩码 */
    private static final int BIT1_MASK = 0b10;


    /**
     * JT/T 消息体【0x8606 路线】编码解码器 // 2019 modify
     *
     * @author togger
     */
    public static final class PointCodec implements Codec<B8606.Point> {

        private PointCodec() {}

        /** 单例 */
        public static final PointCodec INSTANCE = new PointCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8606.Point target) {
            Codec.writeDoubleWord(buf, target.getId());
            Codec.writeDoubleWord(buf, target.getSegmentId());
            Codec.writeDoubleWord(buf, target.getLatitude());
            Codec.writeDoubleWord(buf, target.getLongitude());
            Codec.writeByte(buf, target.getWidth());
            Codec.writeByte(buf, target.getProps());

            if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
                Codec.writeWord(buf, target.getMaxTime());
                Codec.writeWord(buf, target.getMinTime());
            }

            if ((target.getProps() & BIT1_MASK) == BIT1_MASK) {
                Codec.writeWord(buf, target.getMaxSpeed());
                Codec.writeByte(buf, target.getDuration());

                if (version > 0) {
                    Codec.writeWord(buf, target.getNightMaxSpeed());
                }
            }
        }

        @Override
        public void decode(int version, ByteBuf buf, B8606.Point target) {
            target.setId(Codec.readDoubleWord(buf));
            target.setSegmentId(Codec.readDoubleWord(buf));
            target.setLatitude(Codec.readDoubleWord(buf));
            target.setLongitude(Codec.readDoubleWord(buf));
            target.setWidth(Codec.readByte(buf));
            target.setProps(Codec.readByte(buf));

            if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
                target.setMaxTime(Codec.readWord(buf));
                target.setMinTime(Codec.readWord(buf));
            }

            if ((target.getProps() & BIT1_MASK) == BIT1_MASK) {
                target.setMaxSpeed(Codec.readWord(buf));
                target.setDuration(Codec.readByte(buf));

                if (version > 0) {
                    target.setNightMaxSpeed(Codec.readWord(buf));
                }
            }
        }

        @Override
        public B8606.Point newInstance() {
            return new B8606.Point();
        }

    }

}
