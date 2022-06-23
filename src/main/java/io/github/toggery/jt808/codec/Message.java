package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.AbstractToStringJoiner;
import io.github.toggery.jt808.messagebody.B8001;
import io.github.toggery.jt808.messagebody.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JT/T 消息
 *
 * <br><br>
 * <p>
 * 消息结构如下：
 * <pre>
 * +--------------------------------------------------+
 * | delimiter | header | body | checksum | delimiter |
 * +--------------------------------------------------+
 * </pre>
 *
 * <p>
 * 标识位，即数据帧分隔符 {@code delimiter}，采用 {@code 0x7e} 表示，若消息头
 * {@code header}、消息体 {@code body} 及校验码 {@code checksum} 中出现
 * {@code 0x7e} 及 {@code 0x7d}，则要进行转义处理。
 *
 * <p>
 * 转义规则定义如下：
 * <ul>
 *     <li>先将 {@code 0x7d} 转换为固定两字节数据: {@code 0x7d 0x01}</li>
 *     <li>再将 {@code 0x7e} 转换为固定两字节数据: {@code 0x7d 0x02}</li>
 * </ul>
 *
 * <p>
 * 转义处理过程如下：
 * <ul>
 *     <li>发送消息时：封装 {@code encode =>} 计算并填充校验码 {@code sign =>} 转义
 *     {@code escape(0x7d -> 0x7d 0x01, 0x7e -> 0x7d 0x02)}</li>
 *     <li>接收消息时：转义还原 {@code unescape(0x7d 0x02 -> 0x7e, 0x7d 0x01 -> 0x7d) =>} 验证校验码
 *     {@code verify =>} 解析 {@code decode}</li>
 * </ul>
 *
 * @param <B> 消息体类型
 * @author togger
 */
public final class Message<B> extends AbstractToStringJoiner {

    /**
     * 封装（编码）消息，消息数据首尾含有标识位（分隔符）
     *
     * @param msg 要封装（编码）的消息
     * @param bufSupplier 字节缓冲区提供者
     * @param bufConsumer 字节缓冲区消费者
     * @param messageMetadataMap 消息元数据字典
     * @param attributeMap 属性字典
     * @param <T> 消息体类型
     * @throws NullPointerException 如果任何一个参数对象为 {@code null}
     * @throws EncodingException 如果执行过程中出现编码错误
     */
    public static <T> void encode(Message<T> msg, Function<Integer, ByteBuf> bufSupplier
            , Map<Integer, MessageMetadata> messageMetadataMap
            , Consumer<ByteBuf> bufConsumer, AttributeMap attributeMap) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(bufSupplier);
        Objects.requireNonNull(bufConsumer);
        Objects.requireNonNull(attributeMap);

        msg.setBodyLength(0);
        msg.setLongBody(false);
        msg.bodyPacketCount = 0;
        msg.bodyPacketNo = 0;

        // 1. 没有消息体
        if (msg.getBody() == null) {
            msg.setSn(takeSn(attributeMap));
            ByteBuf buf = bufSupplier.apply(estimateHeaderLength(msg.version, false));
            msg.encode(buf);
            buf = escape(sign(buf));
            bufConsumer.accept(buf);
            return;
        }

        final MessageMetadata metadata = Objects.requireNonNull(messageMetadataMap).get(msg.id);
        if (metadata == null) {
            throw new EncodingException("not found metadata of message: " + msg);
        }
        final Codec<T> bodyCodec = castBodyCodec(metadata.getBodyCodec());
        if (bodyCodec == null) {
            throw new EncodingException("no body codec for message: " + msg);
        }

        // 2. 有消息体
        final ByteBuf bodyBuf = bufSupplier.apply(256);
        bodyCodec.encode(msg.version, bodyBuf, msg.getBody());
        int bodyLen = bodyBuf.readableBytes();
        if (bodyLen > LONG_BODY_CAP) {
            throw new EncodingException(String.format("message body length can NOT be more than %,d", LONG_BODY_CAP));
        }
        final int cnt = (int) Math.ceil((double) bodyLen / BODY_LENGTH_MAX);
        if (cnt == 0) {
            bodyBuf.release();
            throw new EncodingException("message body dit NOT encode any thing, body type=" + msg.getBody().getClass());
        }

        // 2.1 不分包
        if (cnt == 1) {
            msg.setBodyLength(bodyLen);
            msg.setSn(takeSn(attributeMap));
            final ByteBuf headerBuf = bufSupplier.apply(estimateHeaderLength(msg.version, false));
            msg.encode(headerBuf);
            ByteBuf buf = bodyBuf.alloc()
                    .compositeBuffer(2)
                    .addComponent(true, headerBuf)
                    .addComponent(true, bodyBuf);
            buf = escape(sign(buf));
            bufConsumer.accept(buf);

            return;
        }

        // 2.2 分包
        try {
            final int headerLen = estimateHeaderLength(msg.version, true);
            msg.setLongBody(true);
            msg.bodyPacketCount = cnt;
            final int[] sns = takeSns(attributeMap, cnt);
            for (int i = 0; i < cnt; i++) {
                bodyLen -= BODY_LENGTH_MAX * i;
                final int actualLen = Math.min(BODY_LENGTH_MAX, bodyLen);
                msg.setBodyLength(actualLen);
                msg.bodyPacketNo = i + 1;
                msg.setSn(sns[i]);
                final ByteBuf headerBuf = bufSupplier.apply(headerLen);
                msg.encode(headerBuf);
                final ByteBuf packetBuf = bodyBuf.readRetainedSlice(actualLen);
                ByteBuf buf = bodyBuf.alloc()
                        .compositeBuffer(2)
                        .addComponent(true, headerBuf)
                        .addComponent(true, packetBuf)
                        ;
                buf = escape(sign(buf));
                bufConsumer.accept(buf);
            }
            msg.setBodyLength(BODY_LENGTH_MAX);
            msg.bodyPacketNo = 1;
            msg.setSn(sns[0]);
        } finally {
            bodyBuf.release();
        }
    }

    /**
     * 解析（解码）消息，消息数据首尾含有标识位（分隔符）
     *
     * @param buf 字节缓冲区
     * @param messageMetadataMap 消息元数据字典
     * @param attributeMap 属性字典
     * @param <T> 消息体类型
     * @return 解析（解码）后的消息
     * @throws NullPointerException 如果参数对象 {@code buf/messageMetadataMap/attributeMap} 中的任何一个为 {@code null}
     * @throws DecodingException 如果执行过程中出现解码错误
     */
    public static <T> Message<T> decode(ByteBuf buf
            , Map<Integer, MessageMetadata> messageMetadataMap, AttributeMap attributeMap) {
        Objects.requireNonNull(buf);
        Objects.requireNonNull(attributeMap);

        if (!buf.isReadable()) return null;

        try {
            int start = buf.readerIndex();
            int end = buf.writerIndex() - 1;
            if (buf.getByte(start) == DELIMITER) {
                start++;
                buf.readerIndex(start);
            }
            if (buf.getByte(end) == DELIMITER) {
                end--;
            }
            final int len = end - start + 1;
            if (len <= 0) return null;
            return decodeWithoutDelimiters(buf.readRetainedSlice(len), messageMetadataMap, attributeMap);
        } finally {
            buf.release();
        }
    }

    /**
     * 解析（解码）消息，消息数据首尾没有标识位（分隔符）
     *
     * @param buf 字节缓冲区
     * @param messageMetadataMap 入站消息元数据字典
     * @param attributeMap 属性字典
     * @param <T> 消息体类型
     * @return 解析（解码）后的消息
     * @throws NullPointerException 如果任何一个参数对象为 {@code null}
     * @throws DecodingException 如果执行过程中出现解码错误
     */
    public static <T> Message<T> decodeWithoutDelimiters(ByteBuf buf
            , Map<Integer, MessageMetadata> messageMetadataMap, AttributeMap attributeMap) {
        Objects.requireNonNull(buf);
        Objects.requireNonNull(attributeMap);

        buf = unescape(buf);
        if (buf == null) {
            return null;
        }

        ByteBuf verified = null, bodyBuf = null;
        try {
            if (!verify(buf)) {
                return null;
            }
            verified = buf.readRetainedSlice(buf.writerIndex() - 1);

            final Message<T> msg = new Message<>();
            msg.decode(verified);
            if (msg.getBodyLength() <= 0) {
                return msg;
            }

            final MessageMetadata metadata = Objects.requireNonNull(messageMetadataMap).get(msg.id);
            if (metadata == null) {
                throw new DecodingException("not found metadata of message: " + msg);
            }
            final Codec<T> bodyCodec = castBodyCodec(metadata.getBodyCodec());
            if (bodyCodec == null) {
                throw new DecodingException("no body codec for message: " + msg);
            }

            if (!msg.isLongBody()) {
                try {
                    msg.body = bodyCodec.decode(msg.version, verified);
                } catch (Exception e) {
                    throw new DecodingException("failed to instantiate body", e);
                }
                return msg;
            }

            bodyBuf = verified.readRetainedSlice(verified.readableBytes());
            Message<T> cached = null;
            final Message<?> gCached = getBodyPackets(attributeMap);
            if (gCached == null) {
                cached = msg;
                msg.bodyPackets = buf.alloc().compositeBuffer(msg.bodyPacketCount);
                setBodyPackets(attributeMap, cached);
            } else if (gCached.id == msg.id) {
                try {
                    cached = castBodyPackets(gCached);
                } catch (ClassCastException ignored) {
                }
            }
            if (cached == null || !cached.addBodyPacket(msg.bodyPacketNo, bodyBuf)) {
                releaseBodyPackets(attributeMap);
                return null;
            }
            if (cached.bodyPackets.numComponents() >= msg.bodyPacketCount) {
                try {
                    cached.body = bodyCodec.decode(cached.version, cached.bodyPackets);
                    return cached;
                } catch (Exception e) {
                    throw new DecodingException("failed to instantiate body", e);
                } finally {
                    releaseBodyPackets(attributeMap);
                }
            }

            return null;
        } finally {
            buf.release();
            if (verified != null) {
                verified.release();
            }
            if (bodyBuf != null) {
                bodyBuf.release();
            }
        }
    }

    /**
     * 实例化消息
     *
     * @param id 消息 ID
     * @param body 消息体
     * @param <T> 消息体类型
     * @return 消息实例
     */
    public static <T> Message<T> of(int id, T body) {
        final Message<T> msg = new Message<>();
        msg.setId(id);
        msg.body = body;
        return msg;
    }

    /**
     * 实例化消息
     *
     * @param id 消息 ID
     * @return 消息实例
     */
    public static Message<?> of(int id) {
        return of(id, null);
    }

    /**
     * 实例化应答消息
     *
     * @param id 消息 ID
     * @param body 消息体
     * @param request 请求消息
     * @param <T> 消息体类型
     * @return 应答消息
     */
    public static <T> Message<T> replyAs(int id, T body, Message<?> request) {
        Objects.requireNonNull(request);

        final Message<T> reply = of(id, body);
        reply.setEncryption(request.getEncryption());
        reply.setVersion(request.version);
        reply.simNo = request.simNo;
        reply.sn = request.sn;
        return reply;
    }

    /**
     * 实例化应答消息
     *
     * @param request 请求消息
     * @return 应答消息
     */
    public static Message<B8001> replyAs8001(Message<?> request) {
        final B8001 body = new B8001();
        final Message<B8001> reply = replyAs(0x8001, body, request);
        body.setReplySn(request.getSn());
        body.setReplyId(request.getId());
        return reply;
    }

    /**
     * 实例化应答消息
     * @param request 请求消息
     * @param result 结果值
     * @return 应对消息
     */
    public static Message<B8001> replyAs8001(Message<?> request, int result) {
        final Message<B8001> reply = replyAs8001(request);
        reply.getBody().setResult(result);
        return reply;
    }


    /** WORD 消息ID */
    private int id;
    /** WORD 消息体属性 */
    private int bodyProps;
    /** BYTE 协议版本号，每次关键修订递增，初始版本为 1 // 2019 new */
    private int version;
    /** BCD[ 6 / 10 ] 终端手机号 */
    private String simNo;
    /** WORD 消息流水号，按发送顺序从 0 开始循环累加 */
    private int sn;

    /** WORD 消息体分包总数 */
    private int bodyPacketCount;
    /** WORD 消息体分包序号，从 1 开始 */
    private int bodyPacketNo;

    /** 关联 ID，不参与编码解码 */
    private String correlationId;

    /** 消息体 */
    private B body;

    /** 消息体分包缓冲区 **/
    private CompositeByteBuf bodyPackets;

    Message() {}

    @Override
    protected void toStringJoiner(StringJoiner joiner) {
        joiner
                .add(HexUtil.wordString("id=", id))
                .add(HexUtil.wordString("bodyProps=", bodyProps))
                .add("versioning=" + isVersioning())
                .add("longBody=" + isLongBody())
                .add("encryption=" + getEncryption())
                .add("bodyLength=" + getBodyLength())
                .add("version=" + version)
                .add("simNo=" + simNo)
                .add(HexUtil.wordString("sn=", sn))
        ;
        if (isLongBody()) {
            joiner.add(HexUtil.wordString("bodyPacketCount=", bodyPacketCount));
            joiner.add(HexUtil.wordString("bodyPacketNo=", bodyPacketNo));
        }
        if (correlationId != null) {
            joiner.add("correlationId=" + correlationId);
        }
        if (body != null) {
            joiner.add("body=" + body);
        }
    }

    /**
     * 获取消息 ID
     *
     * @return WORD 消息 ID
     */
    public int getId() {
        return id;
    }
    /**
     * 设置消息 ID
     *
     * @param id WORD 消息 ID
     * @throws IllegalArgumentException 如果 {@code id} 超出范围 {@code [0, 65535]}
     */
    private void setId(int id) {
        if (id < 0 || id > WORD_MAX) {
            throw new IllegalArgumentException("id should be in range 0 to " + WORD_MAX);
        }
        this.id = id;
    }

    /**
     * 获取是否含有协议版本号
     *
     * @return 是否含有协议版本号
     */
    public boolean isVersioning() {
        return (bodyProps & VERSIONING_MASK) == VERSIONING_MASK;
    }
    /**
     * 设置是否含有协议版本号
     *
     * @param versioning 是否含有协议版本
     */
    private void setVersioning(boolean versioning) {
        if (versioning) {
            bodyProps |= VERSIONING_MASK;
        } else {
            bodyProps &= ~VERSIONING_MASK;
        }
    }

    /**
     * 获取是否为长消息体
     *
     * @return 是否为长消息体
     */
    public boolean isLongBody() {
        return (bodyProps & LONG_BODY_MASK) == LONG_BODY_MASK;
    }
    /**
     * 设置是否为长消息体
     *
     * @param longBody 是否为长消息体
     */
    private void setLongBody(boolean longBody) {
        if (longBody) {
            bodyProps |= LONG_BODY_MASK;
        } else {
            bodyProps &= ~LONG_BODY_MASK;
        }
    }

    /**
     * 获取加密方法
     *
     * @return 加密方法
     */
    public int getEncryption() {
        return (bodyProps & ENCRYPTION_MASK) >> ENCRYPTION_POSITION;
    }
    /**
     * 设置加密方法，暂不支持
     *
     * @param encryption 加密方法
     * @throws IllegalArgumentException 如果 {@code encryption} 超出范围 {@code [0, 7]}
     */
    public void setEncryption(int encryption) {
        if (encryption < 0 || encryption > ENCRYPTION_MAX) {
            throw new IllegalArgumentException("encryption should be in range 0 to " + ENCRYPTION_MAX);
        }
        bodyProps &= ~ENCRYPTION_MASK;
        bodyProps |= (encryption << ENCRYPTION_POSITION);
    }

    /**
     * 获取消息体长度
     *
     * @return 消息体长度
     */
    public int getBodyLength() {
        return bodyProps & BODY_LENGTH_MAX;
    }
    /**
     * 设置消息体长度
     *
     * @param bodyLength 消息体长度
     * @throws IllegalArgumentException 如果 {@code value} 超出范围 {@code [0, 1023]}
     */
    private void setBodyLength(int bodyLength) {
        if (bodyLength < 0 || bodyLength > BODY_LENGTH_MAX) {
            throw new IllegalArgumentException("bodyLength should be in range 0 to " + BODY_LENGTH_MAX);
        }
        bodyProps &= ~BODY_LENGTH_MAX;
        bodyProps |= bodyLength;
    }

    /**
     * 获取协议版本号
     *
     * @return BYTE 协议版本号，每次关键修订递增，初始版本为 1 // 2019 new
     */
    public int getVersion() {
        return version;
    }
    /**
     * 设置协议版本号，{@code version} 是否大于 {@code 0} 决定了 {@link #isVersioning()} 的返回值
     *
     * @param version BYTE 协议版本号，每次关键修订递增，初始版本为 1 // 2019 new
     * @throws IllegalArgumentException 如果 {@code version} 超出范围 {@code [0, 255]}
     */
    public void setVersion(int version) {
        if (version < 0 || version > BYTE_MAX) {
            throw new IllegalArgumentException("version should be in range 0 to " + BYTE_MAX);
        }
        this.version = version;
        setVersioning(version > 0);
    }

    /**
     * 获取终端手机号
     *
     * @return 终端手机号
     */
    public String getSimNo() {
        return simNo;
    }
    /**
     * 设置终端手机号
     *
     * @param simNo 终端手机号
     */
    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    /**
     * 获取消息流水号
     *
     * @return WORD 消息流水号，按发送顺序从 0 开始循环累加
     */
    public int getSn() {
        return sn;
    }
    /**
     * 设置消息流水号
     *
     * @param sn WORD 消息流水号，按发送顺序从 0 开始循环累加
     * @throws IllegalArgumentException 如果 {@code sn} 超出范围 {@code [0, 65535]}
     */
    private void setSn(int sn) {
        if (sn < 0 || sn > WORD_MAX) {
            throw new IllegalArgumentException("sn should be in range 0 to " + WORD_MAX);
        }
        this.sn = sn;
    }

    /**
     * 获取关联 ID
     * @return 关联 ID，不参与编码解码
     */
    public String getCorrelationId() {
        return correlationId;
    }
    /**
     * 设置关联 ID
     *
     * @param correlationId 关联 ID，不参与编码解码
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * 获取消息体
     *
     * @return 消息体
     */
    public B getBody() {
        return body;
    }


    private boolean addBodyPacket(int no, ByteBuf buf) {
        if (bodyPackets == null) return false;
        bodyPackets.addComponent(true, buf.retain());
        return bodyPackets.numComponents() == no;
    }

    private void encode(ByteBuf buf) {
        Codec.writeWord(buf, id);
        Codec.writeWord(buf, bodyProps);

        if (isVersioning()) {
            Codec.writeByte(buf, version);
        }

        Codec.writeBcd(buf, simNo, version > 0 ? 10 : 6);
        Codec.writeWord(buf, sn);

        if (isLongBody()) {
            Codec.writeWord(buf, bodyPacketCount);
            Codec.writeWord(buf, bodyPacketNo);
        }
    }

    private void decode(ByteBuf buf) {
        id = Codec.readWord(buf);
        bodyProps = Codec.readWord(buf);
        version = isVersioning() ? Codec.readByte(buf) : 0;

        simNo = Codec.readBcd(buf, version > 0 ? 10 : 6, true);
        sn = Codec.readWord(buf);

        if (isLongBody()) {
            bodyPacketCount = Codec.readWord(buf);
            bodyPacketNo = Codec.readWord(buf);
        } else {
            bodyPacketCount = 0;
            bodyPacketNo = 0;
        }
    }


    private static int estimateHeaderLength(int version, boolean isLongBody) {
        final int len = version > 0 ? HEADER_LENGTH_MIN_2019 : HEADER_LENGTH_MIN_2013;
        return isLongBody ? len + BODY_PACKET_OPTIONS_LENGTH : len;
    }

    private static int takeSn(AttributeMap attributeMap) {
        return takeSns(attributeMap, 1)[0];
    }

    private static int[] takeSns(AttributeMap attributeMap, int count) {
        final Attribute<Integer> attr = attributeMap.attr(SN_KEY);
        int[] sns = new int[count];
        Integer value;
        for (int i = 0; i < count; i++) {
            value = attr.get();
            if (value == null) {
                value = 0;
            }
            attr.set(value == WORD_MAX ? 0 : value + 1);
            sns[i] = value;
        }
        return sns;
    }

    private static Message<?> getBodyPackets(AttributeMap attributeMap) {
        return attributeMap.attr(BODY_PACKETS_KEY).get();
    }

    private static <T> Message<T> castBodyPackets(Message<?> packets) {
        if (packets == null) return null;

        @SuppressWarnings("unchecked")
        final Message<T> casted = (Message<T>) packets;
        return casted;
    }

    private static <T> Codec<T> castBodyCodec(Codec<?> bodyCodec) {
        if (bodyCodec == null) return null;

        @SuppressWarnings("unchecked")
        final Codec<T> casted = (Codec<T>) bodyCodec;
        return casted;
    }

    private static void setBodyPackets(AttributeMap attributeMap, Message<?> message) {
        attributeMap.attr(BODY_PACKETS_KEY).set(message);
    }

    /**
     * 释放缓存的消息体分包
     *
     * @param attributeMap 属性字典
     */
    public static void releaseBodyPackets(AttributeMap attributeMap) {
        Optional.ofNullable(getBodyPackets(attributeMap)).ifPresent(m -> ReferenceCountUtil.release(m.bodyPackets));
        setBodyPackets(attributeMap, null);
    }

    private static ByteBuf sign(ByteBuf buf) {
        final byte code = bcc(buf, 0);
        buf.writeByte(code);
        return buf;
    }

    private static ByteBuf escape(ByteBuf buf) {
        final ByteBuf delimiter = buf.alloc().buffer(1, 1).writeByte(DELIMITER);
        final ByteBuf esc1 = buf.alloc().buffer(1, 1).writeByte(DELIMITER_ESCAPE_1);
        final ByteBuf esc2 = buf.alloc().buffer(1, 1).writeByte(DELIMITER_ESCAPE_2);
        final List<ByteBuf> list = new ArrayList<>();
        list.add(delimiter);

        final int start = buf.readerIndex();
        final int end = buf.writerIndex();
        int index = start;
        try {
            for (int i = start; i < end; i++) {
                final byte v = buf.getByte(i);
                if (v == DELIMITER_ESCAPE) {
                    list.add(buf.retainedSlice(index, i + 1 - index));
                    list.add(esc1.retain());
                    index = i + 1;
                } else if (v == DELIMITER) {
                    buf.setByte(i, DELIMITER_ESCAPE);
                    list.add(buf.retainedSlice(index, i + 1 - index));
                    list.add(esc2.retain());
                    index = i + 1;
                }
            }

            if (index == start) {
                list.add(buf.retain());
            } else if (index < end) {
                list.add(buf.retainedSlice(index, end - index));
            }
            list.add(delimiter.retain());

            return buf.alloc()
                    .compositeBuffer(list.size())
                    .addComponents(true, list)
                    ;
        } catch (Exception e) {
            list.forEach(ReferenceCounted::release);
            throw e;
        } finally {
            esc2.release();
            esc1.release();
            buf.release();
        }
    }

    private static byte bcc(ByteBuf buf, int tailLen) {
        int start = buf.readerIndex();
        final int stop = buf.writerIndex() - tailLen;
        byte cs = 0;
        while (start < stop) {
            cs ^= buf.getByte(start++);
        }
        return cs;
    }

    private static ByteBuf unescape(ByteBuf buf) {
        int start = buf.readerIndex();
        final int stop = buf.writerIndex();

        int index = buf.indexOf(start, stop, DELIMITER_ESCAPE);
        if (index < 0) {
            return buf.retain();
        }

        ByteBuf newer = null;
        final List<ByteBuf> list = new ArrayList<>();
        try {
            do {
                final int nextIndex = index + 1;
                if (nextIndex >= stop) {
                    return null;
                }
                final int next = buf.getByte(nextIndex);
                if (next == DELIMITER_ESCAPE_2) {
                    buf.setByte(index, DELIMITER);
                    list.add(buf.retainedSlice(start, nextIndex - start));
                } else if (next == DELIMITER_ESCAPE_1) {
                    list.add(buf.retainedSlice(start, nextIndex - start));
                } else {
                    return null;
                }
                start = nextIndex + 1;
                index = buf.indexOf(start, stop, DELIMITER_ESCAPE);
            } while (index > 0);
            list.add(buf.retainedSlice(start, stop - start));

            newer = buf.alloc()
                    .compositeBuffer(list.size())
                    .addComponents(true, list)
            ;
        } finally {
            if (newer == null) {
                list.forEach(ByteBuf::release);
            }
        }
        return newer;
    }

    private static boolean verify(ByteBuf buf) {
        final byte code = bcc(buf, 1);
        return code == buf.getByte(buf.writerIndex() - 1);
    }


    /** 标识位，即数据帧分隔符 */
    public final static byte DELIMITER           = 0x7e;
    private final static byte DELIMITER_ESCAPE   = 0x7d;
    private final static byte DELIMITER_ESCAPE_1 = 0x01;
    private final static byte DELIMITER_ESCAPE_2 = 0x02;

    private final static int BYTE_MAX = (1 << 8) - 1;
    private final static int WORD_MAX = (1 << 16) - 1;
    private final static int ENCRYPTION_POSITION = 10;
    private final static int ENCRYPTION_MAX = (1 << 3) - 1;
    private final static int VERSIONING_MASK = 1 << 14;
    private final static int LONG_BODY_MASK = 1 << 13;
    private final static int ENCRYPTION_MASK = ENCRYPTION_MAX << ENCRYPTION_POSITION;
    private final static int BODY_LENGTH_MAX = (1 << ENCRYPTION_POSITION) - 1;
    /** 长消息体容量 */
    private final static int LONG_BODY_CAP = BODY_LENGTH_MAX * WORD_MAX;

    /** 消息头长度最小值，2013 版（version=0）且没有消息体 */
    private final static int HEADER_LENGTH_MIN_2013 = 12;
    /** 消息头长度最小值，2019 版（version=1）且没有消息体 */
    private final static int HEADER_LENGTH_MIN_2019 = 17;
    /** 消息体分包封装项长度 */
    private final static int BODY_PACKET_OPTIONS_LENGTH = 4;
    /** 消息头长度最大值，2019 版（version=1）且有消息体 */
    private final static int HEADER_LENGTH_MAX = HEADER_LENGTH_MIN_2019 + BODY_PACKET_OPTIONS_LENGTH;

    /** 数据帧长度最大值：{@code (消息头 + 消息体 + 校验码[1]) * 2(转义预留)} */
    public final static int FRAME_LENGTH_MAX = (HEADER_LENGTH_MAX + BODY_LENGTH_MAX + 1) * 2;
    /** 数据帧长度最小值：2013 版（version=0）且没有消息体 */
    public final static int FRAME_LENGTH_MIN = HEADER_LENGTH_MIN_2013;

    private final static AttributeKey<Integer> SN_KEY = AttributeKey.newInstance("jt808-codec-sn");
    private final static AttributeKey<Message<?>> BODY_PACKETS_KEY = AttributeKey.newInstance("jt808-codec-body-packets");

}
