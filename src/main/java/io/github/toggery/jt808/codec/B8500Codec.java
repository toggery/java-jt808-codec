package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8500;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JT/T 消息体【0x8500 车辆控制】编码解码器 // 2019 modify
 *
 * @author togger
 */
public final class B8500Codec implements Codec<B8500> {

    private B8500Codec() {}

    /** 单例 */
    public static final B8500Codec INSTANCE = new B8500Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8500 target) {
        encodeBase(version, buf, target);
        encodeParams(version, buf, target, null);
    }

    @Override
    public void decode(int version, ByteBuf buf, B8500 target) {
        decodeBase(version, buf, target);
        decodeParams(version, buf, target, null);
    }

    @Override
    public B8500 newInstance() {
        return new B8500();
    }

    /**
     * 编码基本信息
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要编码的对象
     */
    public void encodeBase(int version, ByteBuf buf, B8500 target) {
        if (version > 0) return;

        Codec.writeByte(buf, target.getCommand());
    }

    /**
     * 编码参数列表
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要编码的对象
     * @param others 其他要编码的方法，可以为 {@code null}
     * @param <S> 要编码的对象类型
     */
    public <S extends B8500> void encodeParams(int version, ByteBuf buf, S target, FieldsEncoder<Integer, S> others) {
        if (version < 1) return;

        Codec.writeCountHeadedContent(buf, IntUnit.WORD, target, (b, v) -> {
            final CountedFieldEncoder<Integer> encoder = new CountedFieldEncoder<>(b, Codec::writeWord);
            PARAMS.values().forEach(f -> f.encode(version, encoder, v));
            if (others != null) {
                others.encode(version, encoder, v);
            }
            return encoder.getCount();
        });
    }

    /**
     * 解码基本信息
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     */
    public void decodeBase(int version, ByteBuf buf, B8500 target) {
        if (version > 0) return;

        target.setCommand(Codec.readByte(buf));
    }

    /**
     * 解码参数列表
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     * @param other 其他要解码的方法，可以为 {@code null}
     * @param <S> 要解码的对象类型
     */
    public <S extends B8500> void decodeParams(int version, ByteBuf buf, S target, FieldDecoder<Integer, S> other) {
        if (version < 1) return;

        target.setUnknownParams(null);

        int cnt = Codec.readWord(buf);
        while (cnt-- > 0) {
            final int id = Codec.readWord(buf);
            final FieldCodec<Integer, B8500, ?> param = PARAMS.get(id);
            if (param != null) {
                param.decode(version, buf, target);
            } else if (other == null || !other.decode(id, version, buf, target)) {
                target.putUnknownParam(id, ByteBufUtil.hexDump(buf));
                // ！！！没有长度头，无法继续！！！
                break;
            }
        }
    }


    private static final Map<Integer, FieldCodec<Integer, B8500, ?>> PARAMS = new LinkedHashMap<>();

    private static <V> void register(FieldCodec<Integer, B8500, V> fieldCodec) {
        PARAMS.put(fieldCodec.getId(), fieldCodec);
    }

    static {

        // 0x0001 车门 BYTE 0.车门锁闭 1.车门开启
        register(FieldCodec.ofByte(0x0001, B8500::getX0001, B8500::setX0001));

    }

}
