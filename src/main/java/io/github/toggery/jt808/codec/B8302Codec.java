package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8302;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Objects;

/**
 * JT/T 消息体编码解码器：0x8302 提问下发 // 2019 del
 *
 * @author togger
 */
public final class B8302Codec implements Codec<B8302> {

    private B8302Codec() {}

    /** 单例 */
    public static final B8302Codec INSTANCE = new B8302Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8302 target) {
        Codec.writeByte(buf, target.getProps());
        Codec.writeString(buf, IntUnit.BYTE, target.getQuestion());

        final List<B8302.Option> options = target.getOptions();
        options.stream().filter(Objects::nonNull).forEach(o -> OptionCodec.INSTANCE.encode(version, buf, o));
    }

    @Override
    public void decode(int version, ByteBuf buf, B8302 target) {
        final List<B8302.Option> options = target.getOptions();
        options.clear();

        target.setProps(Codec.readByte(buf));
        target.setQuestion(Codec.readString(buf, IntUnit.BYTE));

        while (buf.isReadable()) {
            options.add(OptionCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B8302 newInstance() {
        return new B8302();
    }


    /**
     * JT/T 消息体编码解码器：0x8302 提问 // 2019 del
     *
     * @author togger
     */
    public static final class OptionCodec implements Codec<B8302.Option> {

        private OptionCodec() {}

        /** 单例 */
        public static final OptionCodec INSTANCE = new OptionCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8302.Option target) {
            Codec.writeByte(buf, target.getId());
            Codec.writeString(buf, IntUnit.WORD, target.getContent());
        }

        @Override
        public void decode(int version, ByteBuf buf, B8302.Option target) {
            target.setId(Codec.readByte(buf));
            target.setContent(Codec.readString(buf, IntUnit.WORD));
        }

        @Override
        public B8302.Option newInstance() {
            return new B8302.Option();
        }

    }

}
