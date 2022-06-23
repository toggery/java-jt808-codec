package io.github.toggery.jt808.codec;

/**
 * JT/T 填充字符
 *
 * @author togger
 */
public enum PadChar {

    /** 字符 空 */
    NUL((byte) 0x00),

    /** 字符 空格 */
    SPACE((byte) 0x20),

    /** 字符 零 */
    ZERO((byte) 0x30);

    /**
     * 获取值
     *
     * @return 值
     */
    public byte getValue() {
        return value;
    }

    private final byte value;
    PadChar(byte value) {
        this.value = value;
    }

}
