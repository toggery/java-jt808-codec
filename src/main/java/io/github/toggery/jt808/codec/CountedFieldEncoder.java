package io.github.toggery.jt808.codec;

import io.netty.buffer.ByteBuf;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * JT/T 累计计数字段编码器
 *
 * @param <I> 字段 {@code ID} 类型
 *
 * @author togger
 */
public class CountedFieldEncoder<I> {

    /**
     * 实例化一个 {@link CountedFieldEncoder}
     * @param buf 字节缓冲区
     * @param idEncoder 字段 {@code ID} 编码方法
     * @throws NullPointerException 如果参数 {@code buf/idEncoder} 中的任何一个为 {@code null}
     */
    public CountedFieldEncoder(ByteBuf buf, BiConsumer<ByteBuf, I> idEncoder) {
        this.buf = Objects.requireNonNull(buf);
        this.idEncoder = Objects.requireNonNull(idEncoder);
    }

    /**
     * 编码字段 {@code ID} 及其值
     * @param id 字段 {@code ID}
     * @param value 字段值
     * @param valueEncoder 字段值编码器
     * @param <V> 字段值类型
     * @throws NullPointerException 如果参数 {@code id/valueEncoder} 中的任何一个为 {@code null}
     */
    public <V> void encode(I id, V value, BiConsumer<ByteBuf, V> valueEncoder) {
        if (value == null) return;

        Objects.requireNonNull(id);
        Objects.requireNonNull(valueEncoder);

        idEncoder.accept(buf, id);
        encodeValue(buf, value, valueEncoder);
        count++;
    }

    /**
     * 获取已编码字段的数量
     *
     * @return 已编码字段的数量
     */
    public int getCount() {
        return count;
    }

    /** 重置 {@link #count} 为零 */
    public void reset() {
        count = 0;
    }

    /**
     * 编码字段值
     * @param buf 字节缓冲区
     * @param value 要编码的字段值
     * @param valueEncoder 字段值编码方法
     * @param <V> 字段值类型
     */
    protected <V> void encodeValue(ByteBuf buf, V value, BiConsumer<ByteBuf, V> valueEncoder) {
        valueEncoder.accept(buf, value);
    }


    private final ByteBuf buf;
    private final BiConsumer<ByteBuf, I> idEncoder;

    private int count;

}
