package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8602;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体编码解码器：0x8602 设置矩形区域 // 2019 modify
 *
 * @author togger
 */
public final class B8602Codec implements Codec<B8602> {

    private B8602Codec() {}

    /** 单例 */
    public static final B8602Codec INSTANCE = new B8602Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8602 target) {
        Codec.writeByte(buf, target.getAction());
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, target.getRegions(), (b, that) -> {
            int count = 0;
            for (final B8602.Region region : that) {
                if (region == null) continue;
                RegionCodec.INSTANCE.encode(version, b, region);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B8602 target) {
        final List<B8602.Region> regions = target.getRegions();
        regions.clear();
        target.setAction(Codec.readByte(buf));

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            regions.add(RegionCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B8602 newInstance() {
        return new B8602();
    }


    /** {@link B8602.Region#getProps()} 二进制位 0 掩码 */
    private static final int BIT0_MASK = 0b1;
    /** {@link B8602.Region#getProps()} 二进制位 1 掩码 */
    private static final int BIT1_MASK = 0b10;


    /**
     * JT/T 消息体编码解码器：0x8602 矩形区域 // 2019 modify
     *
     * @author togger
     */
    public static final class RegionCodec implements Codec<B8602.Region> {

        private RegionCodec() {}

        /** 单例 */
        public static final RegionCodec INSTANCE = new RegionCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8602.Region target) {
            Codec.writeDoubleWord(buf, target.getId());
            Codec.writeWord(buf, target.getProps());
            Codec.writeDoubleWord(buf, target.getLatitudeTopLeft());
            Codec.writeDoubleWord(buf, target.getLongitudeTopLeft());
            Codec.writeDoubleWord(buf, target.getLatitudeBottomRight());
            Codec.writeDoubleWord(buf, target.getLongitudeBottomRight());

            if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
                Codec.writeBcd(buf, target.getStartTime(), 6);
                Codec.writeBcd(buf, target.getEndTime(), 6);
            }

            if ((target.getProps() & BIT1_MASK) == BIT1_MASK) {
                Codec.writeWord(buf, target.getMaxSpeed());
                Codec.writeByte(buf, target.getDuration());

                if (version > 0) {
                    Codec.writeWord(buf, target.getNightMaxSpeed());
                }
            }

            if (version > 0) {
                Codec.writeString(buf, IntUnit.WORD, target.getName());
            }
        }

        @Override
        public void decode(int version, ByteBuf buf, B8602.Region target) {
            target.setId(Codec.readDoubleWord(buf));
            target.setProps(Codec.readWord(buf));
            target.setLatitudeTopLeft(Codec.readDoubleWord(buf));
            target.setLongitudeTopLeft(Codec.readDoubleWord(buf));
            target.setLatitudeBottomRight(Codec.readDoubleWord(buf));
            target.setLongitudeBottomRight(Codec.readDoubleWord(buf));

            if ((target.getProps() & BIT0_MASK) == BIT0_MASK) {
                target.setStartTime(Codec.readBcd(buf,6, false));
                target.setEndTime(Codec.readBcd(buf,6, false));
            }

            if ((target.getProps() & BIT1_MASK) == BIT1_MASK) {
                target.setMaxSpeed(Codec.readWord(buf));
                target.setDuration(Codec.readByte(buf));

                if (version > 0) {
                    target.setNightMaxSpeed(Codec.readWord(buf));
                }
            }

            if (version > 0) {
                target.setName(Codec.readString(buf, IntUnit.WORD));
            }
        }

        @Override
        public B8602.Region newInstance() {
            return new B8602.Region();
        }

    }

}
