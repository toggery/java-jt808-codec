package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0200;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JT/T 消息体编码解码器：0x0200 位置信息汇报 // 2019 modify
 *
 * @author togger
 */
public final class B0200Codec implements Codec<B0200> {

    private B0200Codec() {}

    /** 单例 */
    public static final B0200Codec INSTANCE = new B0200Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0200 target) {
        encodeBase(version, buf, target);
        encodeExtras(version, buf, target, null);
    }

    @Override
    public void decode(int version, ByteBuf buf, B0200 target) {
        decodeBase(version, buf, target);
        decodeExtras(version, buf, target, null);
    }

    @Override
    public B0200 newInstance() {
        return new B0200();
    }

    /**
     * 编码基本信息
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要编码的对象
     */
    public void encodeBase(int version, ByteBuf buf, B0200 target) {
        Codec.writeDoubleWord(buf, target.getAlarmBits());
        Codec.writeDoubleWord(buf, target.getStatusBits());
        Codec.writeDoubleWord(buf, target.getLatitude());
        Codec.writeDoubleWord(buf, target.getLongitude());
        Codec.writeWord(buf, target.getAltitude());
        Codec.writeWord(buf, target.getSpeed());
        Codec.writeWord(buf, target.getDirection());
        Codec.writeBcd(buf, target.getTime(), 6);
    }

    /**
     * 编码附加信息列表
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要编码的对象
     * @param others 其他要编码的方法，可以为 {@code null}
     * @param <S> 要编码的对象类型
     */
    public <S extends B0200> void encodeExtras(int version, ByteBuf buf, S target, FieldsEncoder<Integer, S> others) {
        final CountedFieldEncoder<Integer> encoder =
                new CountedLengthHeadedFieldEncoder<>(buf, Codec::writeByte, IntUnit.BYTE);
        EXTRAS.values().forEach(f -> f.encode(version, encoder, target));
        if (others != null) {
            others.encode(version, encoder, target);
        }
    }

    /**
     * 解码基本信息
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     */
    public void decodeBase(int version, ByteBuf buf, B0200 target) {
        target.setAlarmBits(Codec.readDoubleWord(buf));
        target.setStatusBits(Codec.readDoubleWord(buf));
        target.setLatitude(Codec.readDoubleWord(buf));
        target.setLongitude(Codec.readDoubleWord(buf));
        target.setAltitude(Codec.readWord(buf));
        target.setSpeed(Codec.readWord(buf));
        target.setDirection(Codec.readWord(buf));
        target.setTime(Codec.readBcd(buf, 6, false));
    }

    /**
     * 解码附加信息列表
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     * @param other 其他要解码的方法，可以为 {@code null}
     * @param <S> 要解码的对象类型
     */
    public <S extends B0200> void decodeExtras(int version, ByteBuf buf, S target, FieldDecoder<Integer, S> other) {
        target.setUnknownExtras(null);

        while (buf.isReadable()) {
            final int id = Codec.readByte(buf);
            final ByteBuf fieldBuf = Codec.readSlice(buf, IntUnit.BYTE);
            final FieldCodec<Integer, B0200, ?> extra = EXTRAS.get(id);
            if (extra != null) {
                extra.decode(version, fieldBuf, target);
            } else if (other == null || !other.decode(id, version, fieldBuf, target)) {
                target.putUnknownExtra(id, ByteBufUtil.hexDump(fieldBuf));
            }
        }
    }


    private static final Map<Integer, FieldCodec<Integer, B0200, ?>> EXTRAS = new LinkedHashMap<>();

    private static <V> void register(FieldCodec<Integer, B0200, V> fieldCodec) {
        EXTRAS.put(fieldCodec.getId(), fieldCodec);
    }

    static {

        // 0x01 DWORD 里程，单位为 0.1km，对应车上里程表读数
        register(FieldCodec.ofDoubleWord(0x01, B0200::getX01, B0200::setX01));
        // 0x02 WORD 油量，单位为 0.1L，对应车上油量表读数
        register(FieldCodec.ofWord(0x02, B0200::getX02, B0200::setX02));
        // 0x03 WORD 行驶记录功能获取的速度，0.1km/h
        register(FieldCodec.ofWord(0x03, B0200::getX03, B0200::setX03));

        // ! 2013 add: 0x04
        // 0x04 WORD 需要人工确认报警事件的 ID，从 1 开始计数
        register(FieldCodec.ofWord(0x04, B0200::getX04, B0200::setX04));

        // ! 2019 add: 0x05~06
        // 0x05 BYTE[30] 胎压，单位为 Pa，标定轮子的顺序为从车头开始从左到右顺序排列，定长 30 字节，多余的字节为 0xFF，表示无效数据
        register(FieldCodec.of(0x05, B0200::getX05, B0200::setX05,
                ver -> ver > 0 ? (b, v) -> Codec.writeBytes(b, v, -30, PadChar.NUL) : null,
                ver -> ver > 0 ? b -> Codec.readBytes(b, 30, PadChar.NUL) : null
        ));
        // 0x06 SHORT 车厢温度，单位为摄氏度，取值范围为 -32767 ~ 32767，最高位为 1 表示负数 // 2019 new
        register(FieldCodec.of(0x06, B0200::getX06, B0200::setX06,
                ver -> ver > 0 ? Codec::writeShort : null,
                ver -> ver > 0 ? Codec::readShort : null
        ));

        // 0x07-0x10 保留

        // 0x11 OBJECT 超速报警附加信息见表 28
        register(FieldCodec.of(0x11, B0200::getX11, B0200::setX11, B0200X11Codec.INSTANCE));
        // 0x12 OBJECT 进出区域/路线报警附加信息见表 29
        register(FieldCodec.of(0x12, B0200::getX12, B0200::setX12, B0200X12Codec.INSTANCE));
        // 0x13 OBJECT 路段行驶时间不足/过长报警附加信息见表 30
        register(FieldCodec.of(0x13, B0200::getX13, B0200::setX13, B0200X13Codec.INSTANCE));

        // ! 2011 截至到以上

        // ! 2013 add: 以下全部

        // 0x14-0x24 保留

        // 0x25 DWORD 扩展车辆信号状态位，参数项格式和定义见表 31
        register(FieldCodec.ofDoubleWord(0x25, B0200::getX25, B0200::setX25));

        // 0x25-0x29 保留

        // 0x2A WORD I0 状态位，参数项格式和定义见表 32
        register(FieldCodec.ofWord(0x2A, B0200::getX2A, B0200::setX2A));
        // 0x2B DWORD 模拟量，bit[0~15]，AD0;bit[l6~31]，AD1
        register(FieldCodec.ofDoubleWord(0x2B, B0200::getX2B, B0200::setX2B));

        // 0x2C-0x2F 保留

        // 0x30 BYTE 无线通信网络信号强度
        register(FieldCodec.ofByte(0x30, B0200::getX30, B0200::setX30));
        // 0x31 BYTE GNSS定位卫星数
        register(FieldCodec.ofByte(0x31, B0200::getX31, B0200::setX31));

        // 0xE0 后续信息长度 后续自定义信息长度
        // 0xE1-0xFF 自定义区域
    }

}
