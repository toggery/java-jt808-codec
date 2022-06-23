package io.github.toggery.jt808.codec;

import io.netty.buffer.ByteBuf;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * JT/T 含前置字段值长度的累计计数字段编码器
 *
 * @param <I> 字段 {@code ID} 类型
 * @author togger
 */
public class CountedLengthHeadedFieldEncoder<I> extends CountedFieldEncoder<I> {

    /**
     * 实例化一个 {@link CountedLengthHeadedFieldEncoder}
     * @param buf 字节缓冲区
     * @param idEncoder 字段 {@code ID} 编码方法
     * @param lengthUnit 字段值长度单位
     * @throws NullPointerException 如果参数 {@code buf/idEncoder/lengthUnit} 中的任何一个为 {@code null}
     */
    public CountedLengthHeadedFieldEncoder(ByteBuf buf, BiConsumer<ByteBuf, I> idEncoder, IntUnit lengthUnit) {
        super(buf, idEncoder);
        this.lengthUnit = Objects.requireNonNull(lengthUnit);
    }

    @Override
    protected <V> void encodeValue(ByteBuf buf, V value, BiConsumer<ByteBuf, V> valueEncoder) {
        Codec.writeLengthHeadedContent(buf, lengthUnit, value, valueEncoder);
    }


    private final IntUnit lengthUnit;

}
