package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8303;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体编码解码器：0x8303 信息点播菜单设置 // 2019 del
 *
 * @author togger
 */
public final class B8303Codec implements Codec<B8303> {

    private B8303Codec() {}

    /** 单例 */
    public static final B8303Codec INSTANCE = new B8303Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8303 target) {
        Codec.writeByte(buf, target.getType());
        if (target.getType() == 0) {
            return;
        }

        final List<B8303.News> newses = target.getNewses();
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, newses, (b, that) -> {
            int count = 0;
            for (final B8303.News news : that) {
                if (news == null) continue;
                NewsCodec.INSTANCE.encode(version, b, news);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B8303 target) {
        final List<B8303.News> newses = target.getNewses();
        newses.clear();

        target.setType(Codec.readByte(buf));
        if (target.getType() == 0) {
            return;
        }

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            newses.add(NewsCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B8303 newInstance() {
        return new B8303();
    }


    /**
     * JT/T 消息体编码解码器：0x8303 信息点播菜单 // 2019 del
     *
     * @author togger
     */
    public static final class NewsCodec implements Codec<B8303.News> {

        private NewsCodec() {}

        /** 单例 */
        public static final NewsCodec INSTANCE = new NewsCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8303.News target) {
            Codec.writeByte(buf, target.getType());
            Codec.writeString(buf, IntUnit.WORD, target.getName());
        }

        @Override
        public void decode(int version, ByteBuf buf, B8303.News target) {
            target.setType(Codec.readByte(buf));
            target.setName(Codec.readString(buf, IntUnit.WORD));
        }

        @Override
        public B8303.News newInstance() {
            return new B8303.News();
        }

    }

}
