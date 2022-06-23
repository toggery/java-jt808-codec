package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8600;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体【0x8600 设置圆形区域】编码解码器 // 2019 modify
 * <br>
 * <p><b>注：</b>本条消息协议支持周期时间范围，如要限制每天的8:30-18:00，起始/结束时间设
 * 为：000000083000/000000180000，其他以此类推。</p>
 *
 * @author togger
 */
public final class B8600Codec implements Codec<B8600> {

    private B8600Codec() {}

    /** 单例 */
    public static final B8600Codec INSTANCE = new B8600Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8600 target) {
        Codec.writeByte(buf, target.getAction());
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, target.getRegions(), (b, that) -> {
            int count = 0;
            for (final B8600.Region region : that) {
                if (region == null) continue;
                RegionCodec.INSTANCE.encode(version, b, region);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B8600 target) {
        final List<B8600.Region> regions = target.getRegions();
        regions.clear();
        target.setAction(Codec.readByte(buf));

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            regions.add(RegionCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B8600 newInstance() {
        return new B8600();
    }


    /** {@link B8600.Region#getProps()} 二进制位 0 掩码 */
    private static final int BIT0_MASK = 0b1;
    /** {@link B8600.Region#getProps()} 二进制位 1 掩码 */
    private static final int BIT1_MASK = 0b10;


    /**
     * JT/T 消息体【0x8600 圆形区域】编码解码器 // 2019 modify
     *
     * @author togger
     */
    public static final class RegionCodec implements Codec<B8600.Region> {

        private RegionCodec() {}

        /** 单例 */
        public static final RegionCodec INSTANCE = new RegionCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8600.Region target) {
            Codec.writeDoubleWord(buf, target.getId());
            Codec.writeWord(buf, target.getProps());
            Codec.writeDoubleWord(buf, target.getLatitude());
            Codec.writeDoubleWord(buf, target.getLongitude());
            Codec.writeDoubleWord(buf, target.getRadius());

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
        public void decode(int version, ByteBuf buf, B8600.Region target) {
            target.setId(Codec.readDoubleWord(buf));
            target.setProps(Codec.readWord(buf));
            target.setLatitude(Codec.readDoubleWord(buf));
            target.setLongitude(Codec.readDoubleWord(buf));
            target.setRadius(Codec.readDoubleWord(buf));

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
        public B8600.Region newInstance() {
            return new B8600.Region();
        }

    }

}
