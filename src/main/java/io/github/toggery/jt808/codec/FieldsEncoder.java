package io.github.toggery.jt808.codec;

/**
 * JT/T 字段列表编码接口
 *
 * @param <I> 字段 {@code ID} 类型
 * @param <T> 目标对象类型
 * @author togger
 */
@FunctionalInterface
public interface FieldsEncoder<I, T> {

    /**
     * 将对象的字段列表编码写入字节缓冲区中
     * @param version 版本号
     * @param countedFieldEncoder 累计计数字段编码器
     * @param target 要编码的对象
     */
    void encode(int version, CountedFieldEncoder<I> countedFieldEncoder, T target);

}
