package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0802;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体【0x0802 存储多媒体数据检索应答】编码解码器
 *
 * @author togger
 */
public class B0802Codec implements Codec<B0802> {

    private B0802Codec() {}

    /** 单例 */
    public static final B0802Codec INSTANCE = new B0802Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0802 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeCountHeadedContent(buf, IntUnit.WORD, target.getMedias(), (b, that) -> {
            int count = 0;
            for (final B0802.Media media: that) {
                if (media == null) continue;
                MediaCodec.INSTANCE.encode(version, b, media);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B0802 target) {
        final List<B0802.Media> medias = target.getMedias();
        medias.clear();

        target.setReplySn(Codec.readWord(buf));
        int cnt = Codec.readWord(buf);
        while (cnt-- > 0) {
            medias.add(MediaCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B0802 newInstance() {
        return new B0802();
    }


    /**
     * JT/T 消息体【0x0802 存储多媒体数据】编码解码器
     *
     * @author togger
     */
    public static final class MediaCodec implements Codec<B0802.Media> {

        private MediaCodec() {}

        /** 单例 */
        public static final MediaCodec INSTANCE = new MediaCodec();

        @Override
        public void encode(int version, ByteBuf buf, B0802.Media target) {
            Codec.writeDoubleWord(buf, target.getId());
            Codec.writeByte(buf, target.getType());
            Codec.writeByte(buf, target.getChannel());
            Codec.writeByte(buf, target.getEvent());

            B0200Codec.INSTANCE.encodeBase(version, buf, target);
        }

        @Override
        public void decode(int version, ByteBuf buf, B0802.Media target) {
            target.setId(Codec.readDoubleWord(buf));
            target.setType(Codec.readByte(buf));
            target.setChannel(Codec.readByte(buf));
            target.setEvent(Codec.readByte(buf));

            B0200Codec.INSTANCE.decodeBase(version, buf, target);
        }

        @Override
        public B0802.Media newInstance() {
            return new B0802.Media();
        }

    }

}
