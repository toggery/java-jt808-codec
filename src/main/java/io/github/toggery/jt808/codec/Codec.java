package io.github.toggery.jt808.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * JT/T 编码解码器接口，包含各种编码解码的工具类方法
 *
 * @param <T> 要编码解码的对象类型
 * @author togger
 */
public interface Codec<T> {

    /**
     * 编码对象
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要编码的对象
     */
    void encode(int version, ByteBuf buf, T target);
    /**
     * 解码对象
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     */
    void decode(int version, ByteBuf buf, T target);
    /**
     * 创建一个 {@link T} 实例
     * @return {@link T} 实例
     */
    T newInstance();
    /**
     * 解码对象
     * @param version 版本号
     * @param buf 字节缓冲区
     * @return 解码后的对象
     */
    default T decode(int version, ByteBuf buf) {
        final T t = newInstance();
        decode(version, buf, t);
        return t;
    }


    /** ASCII 字符集 */
    Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
    /** STRING(GBK) 字符集 */
    Charset STRING_CHARSET = Charset.forName("GBK");
    /** 可见字符最小值 {@code '!'} */
    byte CHAR_MIN = 0x21;
    /** 可见字符最大值 {@code '~'} */
    byte CHAR_MAX = 0x7E;


    /**
     * 将【字节 BYTE】写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【字节 BYTE】
     */
    static void writeByte(ByteBuf buf, int value) {
        buf.writeByte(value);
    }
    /**
     * 将【字 WORD】写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【字 WORD】
     */
    static void writeWord(ByteBuf buf, int value) {
        buf.writeShort(value);
    }
    /**
     * 将【双字 DWORD】写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【双字 DWORD】
     */
    static void writeDoubleWord(ByteBuf buf, long value) {
        buf.writeInt((int) value);
    }
    /**
     * 将整数写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的整数
     */
    static void writeShort(ByteBuf buf, short value) {
        buf.writeShort(value);
    }
    /**
     * 将整数写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的整数
     */
    static void writeInt(ByteBuf buf, int value) {
        buf.writeInt(value);
    }
    /**
     * 将整数写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的整数
     */
    static void writeLong(ByteBuf buf, long value) {
        buf.writeLong(value);
    }

    /**
     * 执行任务写入字节缓冲区，包括前置长度。
     * @param <V> 值类型
     * @param buf 字节缓冲区
     * @param lengthUnit 长度单位
     * @param value 要传入写入任务的值
     * @param contentWriter 内容写入任务
     */
    static <V> void writeLengthHeadedContent(ByteBuf buf, IntUnit lengthUnit, V value, BiConsumer<ByteBuf, V> contentWriter) {
        Function<Integer, ByteBuf> lengthWriter;
        int maxLength;
        switch (lengthUnit) {
            case BYTE:
                lengthWriter = buf::writeByte;
                maxLength = 0xFF;
                break;
            case WORD:
                lengthWriter = buf::writeShort;
                maxLength = 0xFFFF;
                break;
            case DWORD:
                lengthWriter = buf::writeInt;
                maxLength = Integer.MAX_VALUE;
                break;
            default:
                throw new IllegalArgumentException("unhandled: int unit=" + lengthUnit);
        }

        final int index = buf.writerIndex();
        lengthWriter.apply(0);
        final int start = buf.writerIndex();
        contentWriter.accept(buf, value);
        final int end = buf.writerIndex();
        if (end == start) {
            return;
        }

        final int length = end - start;
        if (length > maxLength) {
            throw new IllegalStateException(String.format("exceeds max length: %,d, actual: %,d", maxLength, length));
        }

        buf.writerIndex(index);
        lengthWriter.apply(length);
        buf.writerIndex(end);
    }
    /**
     * 执行任务写入字节缓冲区，如果写入内容返回有效数量，则写入前置数量。
     * @param <V> 值类型
     * @param buf 字节缓冲区
     * @param countUnit 数量单位
     * @param value 要传入写入任务的值
     * @param contentWriter 内容写入任务
     */
    static <V> void writeCountHeadedContent(ByteBuf buf, IntUnit countUnit, V value, BiFunction<ByteBuf, V, Integer> contentWriter) {
        Function<Integer, ByteBuf> countWriter;
        int maxCount;
        switch (countUnit) {
            case BYTE:
                countWriter = buf::writeByte;
                maxCount = 0xFF;
                break;
            case WORD:
                countWriter = buf::writeShort;
                maxCount = 0xFFFF;
                break;
            case DWORD:
                countWriter = buf::writeInt;
                maxCount = Integer.MAX_VALUE;
                break;
            default:
                throw new IllegalArgumentException("unhandled: int unit=" + countUnit);
        }

        final int index = buf.writerIndex();
        countWriter.apply(0);
        final int count = contentWriter.apply(buf, value);
        if (count <= 0) {
            buf.writerIndex(index);
            return;
        }
        if (count > maxCount) {
            throw new IllegalStateException(String.format("exceeds max count: %,d, actual: %,d", maxCount, count));
        }

        final int end = buf.writerIndex();
        buf.writerIndex(index);
        countWriter.apply(count);
        buf.writerIndex(end);
    }

    /**
     * 将【字符串 STRING】写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【字符串 STRING】
     */
    static void writeString(ByteBuf buf, String value) {
        if (value != null && value.length() > 0) {
            final byte[] bytes = value.getBytes(STRING_CHARSET);
            buf.writeBytes(bytes);
        }
    }
    /**
     * 将【字符串 STRING】写入字节缓冲区，包括前置长度。
     *
     * @param buf 字节缓冲区
     * @param lenUnit 长度单位
     * @param value 要写入的【字符串 STRING】
     */
    static void writeString(ByteBuf buf, IntUnit lenUnit, String value) {
        writeLengthHeadedContent(buf, lenUnit, value, Codec::writeString);
    }

    /**
     * 将【字节数组】写入字节缓冲区。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【字节数组】
     */
    static void writeBytes(ByteBuf buf, byte[] value) {
        if (value != null && value.length > 0) {
            buf.writeBytes(value);
        }
    }
    /**
     * 将【字节数组】写入字节缓冲区，长度不足则填充指定的字节值，超长截断尾部。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【字节数组】
     * @param length 要写入的字节数量，大于零填充左侧（首部），小于零填充右侧（尾部），等于零等同于 {@link #writeBytes(ByteBuf, byte[])}
     * @param padByte 长度不足时填充的字节值
     */
    static void writeBytes(ByteBuf buf, byte[] value, int length, PadChar padByte) {
        if (length == 0) {
            writeBytes(buf, value);
            return;
        }

        final int actual = value != null ? value.length : 0;
        final int pLen = Math.abs(length);
        int pad = pLen - actual;

        if (length > 0) {
            while (pad-- > 0) {
                buf.writeByte(padByte.getValue());
            }
        }
        if (actual > 0) {
            buf.writeBytes(value, 0, Math.min(actual, pLen));
        }
        if (length < 0) {
            while (pad-- > 0) {
                buf.writeByte(padByte.getValue());
            }
        }
    }
    /**
     * 将【字节数组】写入字节缓冲区，包括前置长度。
     *
     * @param buf 字节缓冲区
     * @param lenUnit 长度单位
     * @param value 要写入的【字节数组】
     */
    static void writeBytes(ByteBuf buf, IntUnit lenUnit, byte[] value) {
        writeLengthHeadedContent(buf, lenUnit, value, (BiConsumer<ByteBuf, ? super byte[]>) Codec::writeBytes);
    }

    /**
     * 将【ASCII 字符串】写入字节缓冲区，长度不足则填充指定的字节值，超长截断尾部。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【ASCII 字符串】
     * @param length 要写入的字节数量，大于零填充左侧（首部），小于零填充右侧（尾部），等于零等同于 {@link #writeBytes(ByteBuf, byte[])}
     * @param padChar 长度不足时填充的 ASCII 字符
     */
    static void writeChars(ByteBuf buf, String value, int length, PadChar padChar) {
        final byte[] bytes = value != null && value.length() > 0
                ? value.getBytes(ASCII_CHARSET)
                : null;
        writeBytes(buf, bytes, length, padChar);
    }

    /**
     * 将【BCD 8421 码】写入字节缓冲区，长度不足则在左侧（首部）填充字符 {@link PadChar#ZERO}，超长截断。
     *
     * @param buf 字节缓冲区
     * @param value 要写入的【BCD 8421 码】
     * @param length 要写入的字节数量
     */
    static void writeBcd(ByteBuf buf, String value, int length) {
        byte[] bytes;
        if (value == null || value.isEmpty()) {
            bytes = new byte[0];
        } else {
            // 1个字节可表示2位数字
            final int doubleLength = length * 2;
            // 超长
            if (value.length() > doubleLength) {
                if (value.charAt(0) == PadChar.ZERO.getValue()) {
                    // 前缀是 '0'，截断两侧
                    int start = 1;
                    final int end = value.length() - doubleLength;
                    for (int i = start; i < end; i++) {
                        if (value.charAt(i) != PadChar.ZERO.getValue()) {
                            start = i;
                            break;
                        }
                    }
                    value = value.substring(start, doubleLength);
                } else {
                    // 前缀不是 '0'，截断右侧
                    value = value.substring(0, doubleLength);
                }
            }
            // 长度不是偶数前补 '0'，保证后续十六进制解码正常
            if ((value.length() & 1) != 0) {
                value = ((char) PadChar.ZERO.getValue()) + value;
            }
            bytes = ByteBufUtil.decodeHexDump(value);
        }
        writeBytes(buf, bytes, Math.abs(length), PadChar.NUL);
    }


    /**
     * 从字节缓冲区中读取无符号【字节 BYTE】。
     *
     * @param buf 字节缓冲区
     * @return 无符号【字节 BYTE】
     */
    static int readByte(ByteBuf buf) {
        return buf.readUnsignedByte();
    }
    /**
     * 从字节缓冲区中读取无符号【字 WORD】。
     *
     * @param buf 字节缓冲区
     * @return 无符号【字 WORD】
     */
    static int readWord(ByteBuf buf) {
        return buf.readUnsignedShort();
    }
    /**
     * 从字节缓冲区中读取无符号【双字 DWORD】。
     *
     * @param buf 字节缓冲区
     * @return 无符号【双字 DWORD】
     */
    static long readDoubleWord(ByteBuf buf) {
        return buf.readUnsignedInt();
    }
    /**
     * 从字节缓冲区中读取整数。
     *
     * @param buf 字节缓冲区
     * @return 整数
     */
    static short readShort(ByteBuf buf) {
        return buf.readShort();
    }
    /**
     * 从字节缓冲区中读取整数。
     *
     * @param buf 字节缓冲区
     * @return 整数
     */
    static int readInt(ByteBuf buf) {
        return buf.readInt();
    }
    /**
     * 从字节缓冲区中读取整数。
     *
     * @param buf 字节缓冲区
     * @return 整数
     */
    static long readLong(ByteBuf buf) {
        return buf.readLong();
    }

    /**
     * 从字节缓冲区中读取头部后再读取值。
     *
     * @param <H> 头部类型
     * @param buf 字节缓冲区
     * @param headUnit 头部单位
     * @param contentReader 内容读取任务
     * @return 读取的值
     */
    static <H> H readHeadedContent(ByteBuf buf, IntUnit headUnit, BiFunction<ByteBuf, Integer, H> contentReader) {
        int head;
        switch (headUnit) {
            case BYTE:
                head = buf.readUnsignedByte();
                break;
            case WORD:
                head = buf.readUnsignedShort();
                break;
            case DWORD:
                head = (int) buf.readUnsignedInt();
                break;
            default:
                throw new UnsupportedOperationException("unsupported: headUnit=" + headUnit);
        }
        return contentReader.apply(buf, head);
    }
    /**
     * 从字节缓冲区中读取长度后再返回字节缓冲区切片。
     *
     * @param buf 字节缓冲区
     * @param lenUnit 长度单位
     * @return 字节缓冲区切片
     */
    static ByteBuf readSlice(ByteBuf buf, IntUnit lenUnit) {
        return readHeadedContent(buf, lenUnit, ByteBuf::readSlice);
    }

    /**
     * 从字节缓冲区中读取全部可读的【字符串 STRING】。
     *
     * @param buf 字节缓冲区
     * @return 字符串 STRING
     */
    static String readString(ByteBuf buf) {
        return readString(buf, buf.readableBytes());
    }
    /**
     * 从字节缓冲区中读取【字符串 STRING】。
     *
     * @param buf 字节缓冲区
     * @param length 要读取的字节数量
     * @return 字符串 STRING
     */
    static String readString(ByteBuf buf, int length) {
        if (length <= 0) {
            return "";
        }

        final byte[] bytes = readBytes(buf, length, PadChar.NUL);
        return new String(bytes, STRING_CHARSET);
    }
    /**
     * 从字节缓冲区中读取长度后再读取【字符串 STRING】。
     *
     * @param buf 字节缓冲区
     * @param lenUnit 长度单位
     * @return 字符串 STRING
     */
    static String readString(ByteBuf buf, IntUnit lenUnit) {
        return readHeadedContent(buf, lenUnit, Codec::readString);
    }

    /**
     * 从字节缓冲区中读取全部可读的【字节数组】。
     *
     * @param buf 字节缓冲区
     * @return 字节数组
     */
    static byte[] readBytes(ByteBuf buf) {
        return readBytes(buf, buf.readableBytes());
    }
    /**
     * 从字节缓冲区中读取【字节数组】。
     *
     * @param buf 字节缓冲区
     * @param length 要读取的字节数量
     * @return 字节数组
     */
    static byte[] readBytes(ByteBuf buf, int length) {
        byte[] bytes = new byte[length];
        if (length > 0) {
            buf.readBytes(bytes);
        }
        return bytes;
    }
    /**
     * 从字节缓冲区中读取【字节数组】，去除左右（首尾）的填充字节值。
     *
     * @param buf 字节缓冲区
     * @param length 要读取的字节数量
     * @param trimByte 两边要去除的填充字节值
     * @return 字节数组
     */
    static byte[] readBytes(ByteBuf buf, int length, PadChar trimByte) {
        int start = buf.readerIndex();
        int end = start + length;
        final int offset = end;

        while (start < end && buf.getByte(start) == trimByte.getValue()) {
            start++;
        }
        while (start < end && buf.getByte(end - 1) == trimByte.getValue()) {
            end--;
        }

        final byte[] bytes = new byte[end - start];
        buf.readerIndex(start);
        buf.readBytes(bytes);
        buf.readerIndex(offset);
        return bytes;
    }
    /**
     * 从字节缓冲区中读取长度后再读取【字节数组】。
     *
     * @param buf 字节缓冲区
     * @param lenUnit 长度单位
     * @return 字节数组
     */
    static byte[] readBytes(ByteBuf buf, IntUnit lenUnit) {
        return readHeadedContent(buf, lenUnit, Codec::readBytes);
    }

    /**
     * 从字节缓冲区中读取全部可读的【ASCII 字符串】，去除前后（首尾）的无意义字符。
     *
     * @param buf 字节缓冲区
     * @return ASCII 字符串
     */
    static String readChars(ByteBuf buf) {
        return readChars(buf, buf.readableBytes());
    }
    /**
     * 从字节缓冲区中读取【ASCII 字符串】，去除前后（首尾）的无意义字符。
     *
     * @param buf 字节缓冲区
     * @param length 要读取的字节数量
     * @return ASCII 字符串
     */
    static String readChars(ByteBuf buf, int length) {
        if (length <= 0) {
            return "";
        }

        int start = buf.readerIndex();
        int end = start + length;
        final int offset = end;

        byte ch;
        while (start < end) {
            ch = buf.getByte(start);
            if (ch < CHAR_MIN || ch > CHAR_MAX) {
                start++;
            } else {
                break;
            }
        }
        while (start < end) {
            ch = buf.getByte(end - 1);
            if (ch < CHAR_MIN || ch > CHAR_MAX) {
                end--;
            } else {
                break;
            }
        }

        final byte[] bytes = new byte[end - start];
        buf.readerIndex(start);
        buf.readBytes(bytes);
        buf.readerIndex(offset);
        return new String(bytes, ASCII_CHARSET);
    }

    /**
     * 从字节缓冲区中读取全部可读的【BCD 8421 码】。
     *
     * @param buf 字节缓冲区
     * @param trimStart 是否去除首部的填充空白符 {@link PadChar#ZERO}
     * @return BCD 8421 码
     */
    static String readBcd(ByteBuf buf, boolean trimStart) {
        return readBcd(buf, buf.readableBytes(), trimStart);
    }
    /**
     * 从字节缓冲区中读取【BCD 8421 码】。
     *
     * @param buf 字节缓冲区
     * @param length 要读取的字节数量
     * @param trimStart 是否去除首部的填充空白符 {@link PadChar#ZERO}
     * @return BCD 8421 码
     */
    static String readBcd(ByteBuf buf, int length, boolean trimStart) {
        final byte[] bytes = readBytes(buf, length);
        final String str = ByteBufUtil.hexDump(bytes);
        if (trimStart) {
            final int len = str.length();
            if (len > 0 && str.charAt(0) == PadChar.ZERO.getValue()) {
                for (int i = 1; i < len; i++) {
                    if (str.charAt(i) != PadChar.ZERO.getValue()) {
                        return str.substring(i);
                    }
                }
            }
        }
        return str;
    }

}
