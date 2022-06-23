package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B0805;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体【0x0805 摄像头立即拍摄命令应答】编码解码器
 *
 * @author togger
 */
public final class B0805Codec implements Codec<B0805> {

    private B0805Codec() {}

    /** 单例 */
    public static final B0805Codec INSTANCE = new B0805Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0805 target) {
        Codec.writeWord(buf, target.getReplySn());
        Codec.writeByte(buf, target.getResult());
        if (target.getResult() != 0) {
            return;
        }

        final List<Long> mediaIds = target.getMediaIds();
        Codec.writeCountHeadedContent(buf, IntUnit.WORD, mediaIds, (b, that) -> {
            int count = 0;
            for (final Long mediaId : that) {
                if (mediaId == null) continue;
                Codec.writeDoubleWord(buf, mediaId);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B0805 target) {
        final List<Long> mediaIds = target.getMediaIds();
        mediaIds.clear();

        target.setReplySn(Codec.readWord(buf));
        target.setResult(Codec.readByte(buf));
        if (target.getResult() != 0) {
            return;
        }

        int cnt = Codec.readWord(buf);
        while (cnt-- > 0) {
            mediaIds.add(Codec.readDoubleWord(buf));
        }
    }

    @Override
    public B0805 newInstance() {
        return new B0805();
    }

}
