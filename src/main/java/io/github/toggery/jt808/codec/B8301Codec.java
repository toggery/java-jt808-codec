package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8301;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体编码解码器：0x8301 事件设置 // 2019 del
 *
 * @author togger
 */
public final class B8301Codec implements Codec<B8301> {

    private B8301Codec() {}

    /** 单例 */
    public static final B8301Codec INSTANCE = new B8301Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8301 target) {
        Codec.writeByte(buf, target.getType());
        if (target.getType() == 0) {
            return;
        }

        final List<B8301.Event> events = target.getEvents();
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, events, (b, that) -> {
            int count = 0;
            for (final B8301.Event event : that) {
                if (event == null) continue;
                EventCodec.INSTANCE.encode(version, b, event);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B8301 target) {
        final List<B8301.Event> events = target.getEvents();
        events.clear();

        target.setType(Codec.readByte(buf));
        if (target.getType() == 0) {
            return;
        }

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            events.add(EventCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B8301 newInstance() {
        return new B8301();
    }


    /**
     * JT/T 消息体编码解码器：0x8301 事件 // 2019 del
     *
     * @author togger
     */
    public static final class EventCodec implements Codec<B8301.Event> {

        private EventCodec() {}

        /** 单例 */
        public static final EventCodec INSTANCE = new EventCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8301.Event target) {
            Codec.writeByte(buf, target.getId());
            Codec.writeString(buf, IntUnit.BYTE, target.getContent());
        }

        @Override
        public void decode(int version, ByteBuf buf, B8301.Event target) {
            target.setId(Codec.readByte(buf));
            target.setContent(Codec.readString(buf, IntUnit.BYTE));
        }

        @Override
        public B8301.Event newInstance() {
            return new B8301.Event();
        }

    }

}
