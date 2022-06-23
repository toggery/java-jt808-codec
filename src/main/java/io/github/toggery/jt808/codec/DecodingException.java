package io.github.toggery.jt808.codec;

/**
 * JT/T 解码异常
 *
 * @author togger
 */
public class DecodingException extends RuntimeException {

    /**
     * 实例化一个 {@link DecodingException}
     * @param message 错误消息
     */
    public DecodingException(String message) {
        super(message);
    }

    /**
     * 实例化一个 {@link DecodingException}
     * @param message 错误消息
     * @param cause 异常源
     */
    public DecodingException(String message, Throwable cause) {
        super(message, cause);
    }

}
