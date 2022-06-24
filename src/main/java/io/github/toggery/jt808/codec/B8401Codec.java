package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8401;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体编码解码器：0x8401 设置电话本
 *
 * @author togger
 */
public final class B8401Codec implements Codec<B8401> {

    private B8401Codec() {}

    /** 单例 */
    public static final B8401Codec INSTANCE = new B8401Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8401 target) {
        Codec.writeByte(buf, target.getType());
        if (target.getType() == 0) {
            return;
        }

        final List<B8401.Contact> contacts = target.getContacts();
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, contacts, (b, that) -> {
            int count = 0;
            for (final B8401.Contact contact : that) {
                if (contact == null) continue;
                ContactCodec.INSTANCE.encode(version, b, contact);
                count++;
            }
            return count;
        });
    }

    @Override
    public void decode(int version, ByteBuf buf, B8401 target) {
        final List<B8401.Contact> contacts = target.getContacts();
        contacts.clear();

        target.setType(Codec.readByte(buf));
        if (target.getType() == 0) {
            return;
        }

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            contacts.add(ContactCodec.INSTANCE.decode(version, buf));
        }
    }

    @Override
    public B8401 newInstance() {
        return new B8401();
    }


    /**
     * JT/T 消息体编码解码器：0x8401 电话本
     *
     * @author togger
     */
    public static final class ContactCodec implements Codec<B8401.Contact> {

        private ContactCodec() {}

        /** 单例 */
        public static final ContactCodec INSTANCE = new ContactCodec();

        @Override
        public void encode(int version, ByteBuf buf, B8401.Contact target) {
            Codec.writeByte(buf, target.getType());
            Codec.writeString(buf, IntUnit.BYTE, target.getPhone());
            Codec.writeString(buf, IntUnit.BYTE, target.getName());
        }

        @Override
        public void decode(int version, ByteBuf buf, B8401.Contact target) {
            target.setType(Codec.readByte(buf));
            target.setPhone(Codec.readString(buf, IntUnit.BYTE));
            target.setName(Codec.readString(buf, IntUnit.BYTE));
        }

        @Override
        public B8401.Contact newInstance() {
            return new B8401.Contact();
        }

    }

}
