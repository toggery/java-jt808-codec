package io.github.toggery.jt808.codec;

/**
 * JT/T 编码异常
 *
 * @author togger
 */
public class EncodingException extends RuntimeException {

    /**
     * 实例化一个 {@link EncodingException}
     * @param message 错误消息
     */
    public EncodingException(String message) {
        super(message);
    }

    /**
     * 实例化一个 {@link EncodingException}
     * @param message 错误消息
     * @param cause 异常源
     */
    public EncodingException(String message, Throwable cause) {
        super(message, cause);
    }

}
