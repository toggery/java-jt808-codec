package io.github.toggery.jt808.codec;

import io.netty.buffer.ByteBuf;

/**
 * JT/T 字段解码方法
 *
 * @param <I> 字段 {@code ID} 类型
 * @param <T> 目标对象类型
 * @author togger
 */
public interface FieldDecoder<I, T> {


    /**
     * @param id 字段 {@code ID}
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     * @param <S> 要解码的对象类型
     * @return 是否成功
     */
    <S extends T> boolean decode(I id, int version, ByteBuf buf, S target);

}
