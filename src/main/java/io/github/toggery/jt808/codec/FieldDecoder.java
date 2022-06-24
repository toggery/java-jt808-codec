package io.github.toggery.jt808.codec;

import io.netty.buffer.ByteBuf;

/**
 * JT/T 字段解码接口
 *
 * @param <I> 字段 {@code ID} 类型
 * @param <T> 目标对象类型
 * @author togger
 */
@FunctionalInterface
public interface FieldDecoder<I, T> {


    /**
     * @param id 字段 {@code ID}
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     * @return 是否成功
     */
    boolean decode(I id, int version, ByteBuf buf, T target);

}
