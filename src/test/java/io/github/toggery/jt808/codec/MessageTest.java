package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.AttributeMap;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author togger
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageTest {

    @Test
    @Order(1)
    @DisplayName("1.【有消息体】消息编码解码")
    void withBodyCodec() {
        codecOne("【有消息体】", Message.of(0x8001, b8001()), OUTBOUNDS);
    }

    @Test
    @Order(2)
    @DisplayName("2.【无消息体】消息编码解码")
    void withoutBodyCodec() {
        codecOne("【无消息体】", Message.of(0x0002), INBOUNDS);
    }

    @Test
    @Order(3)
    @DisplayName("3.【长消息体】消息编码解码")
    void longBodyCodec() {
        final byte[] data = new byte[1024];
        Arrays.fill(data, (byte)6);
        final B0900 body = b0900();
        body.setData(data);
        codecOne("【长消息体】", Message.of(0x0900, body), INBOUNDS);
    }

    @Test
    @Order(4)
    @DisplayName("4.【入站】消息编码解码")
    void inboundCodec() {
        final Object[] bodies = new Object[]{
                b0A00(),
                b0001(), b0005(),
                b0100(), b0102(), b0104(), b0107(), b0108(),
                b0200(1L), b0201(),
                b0301(), b0302(), b0303(),
                b0500(),
                b0608(),
                b0700(), b0701(), b0702(), b0704(), b0705(),
                b0800(), b0801(), b0802(), b0805(),
                b0900(), b0901()
        };
        codecMany("【入站】", INBOUNDS, bodies);
    }

    @Test
    @Order(5)
    @DisplayName("5.【出站】消息编码解码")
    void outboundCodec() {
        final Object[] bodies = new Object[]{
                b8A00(),
                b8001(), b8003(), b8004(),
                b8100(), b8103(), b8105(), b8106(), b8108(),
                b8202(), b8203(),
                b8300(), b8301(), b8302(), b8303(), b8304(),
                b8400(), b8401(),
                b8500(),
                b8600(), b8601(), b8602(), b8603(), b8604(), b8605(), b8606(), b8607(), b8608(),
                b8700(), b8701(),
                b8800(), b8801(), b8802(), b8803(), b8804(), b8805(),
                b8900()
        };
        codecMany("【出站】", OUTBOUNDS, bodies);
    }

    @BeforeEach
    void beforeEach() {
        System.out.println();
        System.out.println(" >>> 开始测试 <<<");
    }

    @AfterEach
    void afterEach() {
        System.out.println(" >>> 结束测试 <<< ");
        System.out.println();
    }


    final Map<Integer, MessageMetadata> INBOUNDS = MessageMetadata.inbounds();
    final Map<Integer, MessageMetadata> OUTBOUNDS = MessageMetadata.outbounds();


    static void codecOne(String name, Message<?> msg, Map<Integer, MessageMetadata> messageMetadataMap) {
        System.out.println(name + "消息编码解码");
        System.out.println();
        final List<ByteBuf> buffs = new ArrayList<>();
        final AttributeMap attributeMap = new DefaultAttributeMap();
        codecOne(msg, buffs, messageMetadataMap, attributeMap);
        System.out.println();
    }

    static void codecMany(String name, Map<Integer, MessageMetadata> messageMetadataMap, Object[] bodies) {
        final Collection<MessageMetadata> metadataCollection = messageMetadataMap.values();
        System.out.printf("%s消息编码解码: 共计 %d 项, 其中【无消息体】 %d 项（按照注册顺序）"
                , name, metadataCollection.size(), metadataCollection.size() - bodies.length);
        System.out.println();
        System.out.println();
        final Map<Integer, Object> bodyMap = Arrays.stream(bodies)
                .collect(Collectors.toMap(
                        c -> Integer.parseInt(c.getClass().getSimpleName().substring(1), 16),
                        Function.identity()
                ));
        final List<ByteBuf> buffs = new ArrayList<>();
        final AttributeMap attributeMap = new DefaultAttributeMap();
        int index = 0;
        for (final MessageMetadata metadata : metadataCollection) {
            buffs.clear();

            final Codec<?> bodyCodec = metadata.getBodyCodec();
            System.out.printf("%d/%d: %s, %s"
                    , ++index, metadataCollection.size(), metadata.getName()
                    , bodyCodec == null ? "无消息体" : bodyCodec.getClass().getSimpleName()
            );
            System.out.println();
            final Object body = bodyMap.get(metadata.getId());
            assertTrue((bodyCodec == null && body == null) || (bodyCodec != null && body != null)
                    ,"消息体编码解码器与其实例必须同时为 null 或非 null");
            final Message<?> msg = Message.of(metadata.getId(), body);
            codecOne(msg, buffs, messageMetadataMap, attributeMap);
            System.out.println();
        }
    }

    static void codecOne(Message<?> msg, List<ByteBuf> buffs
            , Map<Integer, MessageMetadata> messageMetadataMap, AttributeMap attributeMap) {
        msg.setSimNo("18912345678");
//            msg.setVersion(1);

        // encode
        System.out.println("编码前: ");
        System.out.println(msg);
        try {
            Message.encode(msg, messageMetadataMap, attributeMap, UnpooledByteBufAllocator.DEFAULT::buffer, buffs::add);
            assertTrue(buffs.size() > 0, "Message.encode 方法应该至少产生一个 ByteBuf 对象");
            System.out.println("编码后: ");
            buffs.forEach(bf -> System.out.println(ByteBufUtil.hexDump(bf)));
        } catch (Exception e) {
            buffs.forEach(ReferenceCountUtil::release);
            throw e;
        }

        // decode
        int count = 0;
        Message<?> decoded;
        for (ByteBuf buff : buffs) {
            count++;
            try {
                decoded = Message.decode(buff, messageMetadataMap, attributeMap);
            } catch (Exception e) {
                buffs.forEach(ReferenceCountUtil::release);
                throw e;
            }
            if (decoded != null) {
                assertEquals(count, buffs.size(), "Message.decode 方法应该消费完所有的 ByteBuf 对象");
                System.out.println("解码后: ");
                System.out.println(decoded);
            }
        }
    }


    static B0A00 b0A00() {
        final byte[] bytes = new byte[128];
        Arrays.fill(bytes, (byte) 1);
        final B0A00 b = new B0A00();
        b.setE(1L);
        b.setN(bytes);
        return b;
    }
    static B0001 b0001() {
        final B0001 b = new B0001();
        b.setReplySn(1);
        b.setReplyId(2);
        b.setResult(B0001.RESULT_UNSUPPORTED);
        return b;
    }
    static B0005 b0005() {
        final B0005 b = new B0005();
        b.setOriginalSn(1);
        b.getBodyPacketSns().addAll(Arrays.asList(1, 2, 3));
        return b;
    }
    static B0100 b0100() {
        final B0100 b = new B0100();
        b.setProvince(1);
        b.setCity(2);
        b.setMaker("maker");
        b.setModel("model");
        b.setId("terminal id");
        b.setPlateColor(1);
        b.setPlateNo("V123456");
        return b;
    }
    static B0102 b0102() {
        final B0102 b = new B0102();
        b.setToken("token");
        b.setImei("imei");
        b.setVersion("version");
        return b;
    }
    static B0104 b0104() {
        final B0104 b = new B0104();
        b.setReplySn(1);
        fillB8103(b);

        return b;
    }
    static B0107 b0107() {
        final B0107 b = new B0107();
        b.setType(256);
        b.setMaker("maker");
        b.setModel("model");
        b.setId("terminal id");
        b.setSimId("89860898608986089860");
        b.setHw("hardware version");
        b.setFm("firmware version");
        b.setGnss(15);
        b.setComm(255);

        return b;
    }
    static B0108 b0108() {
        final B0108 b = new B0108();
        b.setType(255);
        b.setResult(B0108.RESULT_FAILED);

        return b;
    }
    static B0200 b0200(long alarmBits) {
        final B0200 b = new B0200();
        fillB0200(b, alarmBits);
        return b;
    }
    static B0200X11 b0200X11() {
        final B0200X11 b = new B0200X11();
        b.setType(1);
        b.setId(2L);

        return b;
    }
    static B0200X12 b0200X12() {
        final B0200X12 b = new B0200X12();
        b.setType(1);
        b.setId(1L);
        b.setDirection(2);

        return b;
    }
    static B0200X13 b0200X13() {
        final B0200X13 b = new B0200X13();
        b.setId(1L);
        b.setDuration(2);
        b.setResult(3);

        return b;
    }
    static B0201 b0201() {
        final B0201 b = new B0201();
        b.setReplySn(1);
        fillB0200(b, 1L);

        return b;
    }
    static B0301 b0301() {
        final B0301 b = new B0301();
        b.setId(1);

        return b;
    }
    static B0302 b0302() {
        final B0302 b = new B0302();
        b.setReplySn(1);
        b.setId(2);

        return b;
    }
    static B0303 b0303() {
        final B0303 b = new B0303();
        b.setType(1);
        b.setAction(B0303.ACTION_DEMAND);

        return b;
    }
    static B0500 b0500() {
        final B0500 b = new B0500();
        b.setReplySn(1);
        fillB0200(b, 1L);

        return b;
    }
    static B0608 b0608() {
        final B0608 b = new B0608();
        b.setType(B0608.TYPE_CIRCLE);
        b.getCircles().addAll(Arrays.asList(b8600_Region(1L), b8600_Region(2L), b8600_Region(3L)));

        return b;
    }
    static B0700 b0700() {
        final B0700 b = new B0700();
        b.setReplySn(1);
        b.setCommand(2);
        b.setData(new byte[]{1,2,3,4});

        return b;
    }
    static B0701 b0701() {
        final B0701 b = new B0701();
        b.setData(new byte[]{1,2,3,4,5,6});

        return b;
    }
    static B0702 b0702() {
        final B0702 b = new B0702();
        b.setStatus(B0702.STATUS_IC_CARD_INSERTED);
        b.setTime("220616123900");
        b.setResult(B0702.RESULT_FAILED_BY_CARD_LOCKED);
        b.setName("驾驶员姓名");
        b.setLicenseNo("V123456");
        b.setAuthority("北京市公安局");
        b.setValidity("20881230");
        b.setIdCardNo("ID1234567890");

        return b;
    }
    static B0704 b0704() {
        final B0704 b = new B0704();
        b.setType(B0704.TYPE_POST);
        b.getLocations().addAll(Arrays.asList(b0200(1L), b0200(2L), b0200(3L)));

        return b;
    }
    static B0705 b0705() {
        final B0705 b = new B0705();
        b.setTime("1239001234");
        b.getCans().addAll(Arrays.asList(b0705_Can(1L), b0705_Can(2L), b0705_Can(3L)));

        return b;
    }
    static B0705.Can b0705_Can(long id) {
        final B0705.Can b = new B0705.Can();
        b.setId(id);
        b.setData(new byte[]{1,2,3,4,5,6,7,8});

        return b;
    }
    static B0800 b0800() {
        final B0800 b = new B0800();
        b.setId(1L);
        b.setType(B0800.TYPE_AUDIO);
        b.setFormat(B0800.FORMAT_MP3);
        b.setEvent(B0800.EVENT_ALARM_TRIGGERED_BY_ROBBERY);
        b.setChannel(10);

        return b;
    }
    static B0801 b0801() {
        final B0801 b = new B0801();
        b.setId(1L);
        b.setType(B0801.TYPE_AUDIO);
        b.setFormat(B0801.FORMAT_MP3);
        b.setEvent(B0801.EVENT_ALARM_TRIGGERED_BY_ROBBERY);
        b.setChannel(10);
        b.setData(new byte[]{1,2,3,4,5,6});
        fillB0200(b, 1L);

        return b;
    }
    static B0802 b0802() {
        final B0802 b = new B0802();
        b.setReplySn(1);
        b.getMedias().addAll(Arrays.asList(b0802_Media(1L), b0802_Media(2L), b0802_Media(3L)));

        return b;
    }
    static B0802.Media b0802_Media(long id) {
        final B0802.Media b = new B0802.Media();
        b.setId(id);
        b.setType(B8802.TYPE_AUDIO);
        b.setChannel(10);
        b.setEvent(B8802.EVENT_ALARM_TRIGGERED_BY_ROBBERY);
        fillB0200(b, id * 100 + 1);

        return b;
    }
    static B0805 b0805() {
        final B0805 b = new B0805();
        b.setReplySn(1);
        b.setResult(B0805.RESULT_CHANNEL_UNSUPPORTED);
        b.getMediaIds().addAll(Arrays.asList(1L, 2L, 3L));

        return b;
    }
    static B0900 b0900() {
        final B0900 b = new B0900();
        b.setType(B0900.TYPE_RTC_IC_CARD_DATA);
        b.setData(new byte[]{1,2,3,4,5,6});

        return b;
    }
    static B0901 b0901() {
        final B0901 b = new B0901();
        b.setData(new byte[]{1,2,3,4,5,6});

        return b;
    }

    static B8A00 b8A00() {
        final byte[] bytes = new byte[128];
        Arrays.fill(bytes, (byte) 1);
        final B8A00 b = new B8A00();
        b.setE(1L);
        b.setN(bytes);
        return b;
    }
    static B8001 b8001() {
        final B8001 b = new B8001();
        b.setReplySn(1);
        b.setReplyId(2);
        b.setResult(B8001.RESULT_UNSUPPORTED);

        return b;
    }
    static B8003 b8003() {
        final B8003 b = new B8003();
        b.setOriginalSn(1);
        b.getBodyPacketSns().addAll(Arrays.asList(1, 2, 3));

        return b;
    }
    static B8004 b8004() {
        final B8004 b = new B8004();
        b.setTime("220616123900");
        return b;
    }
    static B8100 b8100() {
        final B8100 b = new B8100();
        b.setReplySn(1);
        b.setResult(B8100.RESULT_SUCCESSFUL);
        b.setToken("token");

        return b;
    }
    static B8103 b8103() {
        final B8103 b = new B8103();
        fillB8103(b);
        return b;
    }
    static B8105 b8105() {
        final B8105 b = new B8105();
        b.setCommand(7);
        b.setParam("param");

        return b;
    }
    static B8106 b8106() {
        final B8106 b = new B8106();
        b.addAll(Arrays.asList(1L, 2L, 3L));
        return b;
    }
    static B8108 b8108() {
        final B8108 b = new B8108();
        b.setType(B8108.TYPE_BEIDOU_POSITIONING_MODULE);
        b.setMaker("maker");
        b.setVersion("version");
        b.setData(new byte[]{1,2,3,4,5,6});

        return b;
    }
    static B8202 b8202() {
        final B8202 b = new B8202();
        b.setInterval(1000);
        b.setDuration(3600L);

        return b;
    }
    static B8203 b8203() {
        final B8203 b = new B8203();
        b.setOriginalSn(1);
        b.setTypeBits(255L);

        return b;
    }
    static B8300 b8300() {
        final B8300 b = new B8300();
        b.setProps(7);
        b.setType(B8300.TYPE_SERVICE);
        b.setContent("content");

        return b;
    }
    static B8301 b8301() {
        final B8301 b = new B8301();
        b.setType(B8301.TYPE_MODIFY);
        b.getEvents().addAll(Arrays.asList(b8301_Event(1), b8301_Event(2), b8301_Event(3)));

        return b;
    }
    static B8301.Event b8301_Event(int id) {
        final B8301.Event b = new B8301.Event();
        b.setId(id);
        b.setContent("content");

        return b;
    }
    static B8302 b8302() {
        final B8302 b = new B8302();
        b.setProps(7);
        b.setQuestion("question");
        b.getOptions().addAll(Arrays.asList(b8302_Option(1), b8302_Option(2), b8302_Option(3)));

        return b;
    }
    static B8302.Option b8302_Option(int id) {
        final B8302.Option b = new B8302.Option();
        b.setId(id);
        b.setContent("content");

        return b;
    }
    static B8303 b8303() {
        final B8303 b = new B8303();
        b.setType(B8303.TYPE_MODIFY);
        b.getNewses().addAll(Arrays.asList(b8303_News(1), b8303_News(2), b8303_News(3)));

        return b;
    }
    static B8303.News b8303_News(int type) {
        final B8303.News b = new B8303.News();
        b.setType(type);
        b.setName("name");

        return b;
    }
    static B8304 b8304() {
        final B8304 b = new B8304();
        b.setType(255);
        b.setContent("content");

        return b;
    }
    static B8400 b8400() {
        final B8400 b = new B8400();
        b.setType(B8400.TYPE_LISTENING);
        b.setPhone("phone");

        return b;
    }
    static B8401 b8401() {
        final B8401 b = new B8401();
        b.setType(B8401.TYPE_APPEND);
        b.getContacts().addAll(Arrays.asList(
                b8401_Contact(B8401.Contact.TYPE_CALL_IN),
                b8401_Contact(B8401.Contact.TYPE_CALL_OUT),
                b8401_Contact(B8401.Contact.TYPE_CALL_IN_OUT)
        ));

        return b;
    }
    static B8401.Contact b8401_Contact(int type) {
        final B8401.Contact b = new B8401.Contact();
        b.setType(type);
        b.setPhone("phone");
        b.setName("姓名");

        return b;
    }
    static B8500 b8500() {
        final B8500 b = new B8500();
        b.setCommand(255);
        b.setX0001(1);

        return b;
    }
    static B8600 b8600() {
        final B8600 b = new B8600();
        b.setAction(B8600.ACTION_APPEND);
        b.getRegions().addAll(Arrays.asList(b8600_Region(1L), b8600_Region(2L), b8600_Region(3L)));

        return b;
    }
    static B8600.Region b8600_Region(long id) {
        final B8600.Region b = new B8600.Region();
        b.setId(id);
        b.setProps(10);
        b.setLatitude(11L);
        b.setLongitude(12L);
        b.setRadius(13L);
        b.setStartTime("220616123900");
        b.setEndTime("220616123959");
        b.setMaxSpeed(14);
        b.setDuration(15);
        b.setNightMaxSpeed(16);
        b.setName("圆形名称");

        return b;
    }
    static B8601 b8601() {
        final B8601 b = new B8601();
        b.addAll(Arrays.asList(1L, 2L, 3L));

        return b;
    }
    static B8602 b8602() {
        final B8602 b = new B8602();
        b.setAction(B8602.ACTION_APPEND);
        b.getRegions().addAll(Arrays.asList(b8602_Region(1L), b8602_Region(2L), b8602_Region(3L)));

        return b;
    }
    static B8602.Region b8602_Region(long id) {
        final B8602.Region b = new B8602.Region();
        b.setId(id);
        b.setProps(10);
        b.setLatitudeTopLeft(11L);
        b.setLongitudeTopLeft(12L);
        b.setLatitudeBottomRight(13L);
        b.setLongitudeBottomRight(14L);
        b.setStartTime("220616123900");
        b.setEndTime("220616123959");
        b.setMaxSpeed(15);
        b.setDuration(16);
        b.setNightMaxSpeed(17);
        b.setName("矩形区域名称");

        return b;
    }
    static B8603 b8603() {
        final B8603 b = new B8603();
        b.addAll(Arrays.asList(1L, 2L, 3L));

        return b;
    }
    static B8604 b8604() {
        final B8604 b = new B8604();
        b.setId(1);
        b.setProps(10);
        b.setStartTime("220616123900");
        b.setEndTime("220616123959");
        b.setMaxSpeed(11);
        b.setDuration(12);
        b.setNightMaxSpeed(13);
        b.setName("多边形区域名称");
        b.getPoints().addAll(Arrays.asList(b8604_Point(1L), b8604_Point(2L), b8604_Point(3L)));

        return b;
    }
    static B8604.Point b8604_Point(long latitude) {
        final B8604.Point b = new B8604.Point();
        b.setLatitude(latitude);
        b.setLongitude(2L);

        return b;
    }
    static B8605 b8605() {
        final B8605 b = new B8605();
        b.addAll(Arrays.asList(1L, 2L, 3L));

        return b;
    }
    static B8606 b8606() {
        final B8606 b = new B8606();
        b.setId(1);
        b.setProps(10);
        b.setStartTime("220616123900");
        b.setEndTime("220616123959");
        b.setName("路线名称");
        b.getPoints().addAll(Arrays.asList(b8606_Point(1L), b8606_Point(2L), b8606_Point(3L)));

        return b;
    }
    static B8606.Point b8606_Point(long id) {
        final B8606.Point b = new B8606.Point();
        b.setId(id);
        b.setSegmentId(10L);
        b.setLatitude(11L);
        b.setLongitude(12L);
        b.setWidth(13);
        b.setProps(14);
        b.setMaxTime(15);
        b.setMinTime(16);
        b.setMaxSpeed(17);
        b.setDuration(18);
        b.setNightMaxSpeed(19);

        return b;
    }
    static B8607 b8607() {
        final B8607 b = new B8607();
        b.addAll(Arrays.asList(1L, 2L, 3L));

        return b;
    }
    static B8608 b8608() {
        final B8608 b = new B8608();
        b.setType(B8608.TYPE_RECTANGLE);
        b.getIds().addAll(Arrays.asList(1L, 2L, 3L));

        return b;
    }
    static B8700 b8700() {
        final B8700 b = new B8700();
        b.setCommand(255);
        b.setData(new byte[]{1,2,3,4,5,6,7,8});

        return b;
    }
    static B8701 b8701() {
        final B8701 b = new B8701();
        b.setCommand(255);
        b.setData(new byte[]{1,2,3,4,5,6});

        return b;
    }
    static B8800 b8800() {
        final B8800 b = new B8800();
        b.setId(1L);
        b.getBodyPacketSns().addAll(Arrays.asList(1, 2, 3));

        return b;
    }
    static B8801 b8801() {
        final B8801 b = new B8801();
        b.setChannel(10);
        b.setCommand(B8801.COMMAND_PICTURE_RECORDING);
        b.setInterval(11);
        b.setAction(B8801.ACTION_SAVE);
        b.setResolution(12);
        b.setQuality(13);
        b.setBrightness(14);
        b.setContrast(15);
        b.setSaturation(16);
        b.setChroma(17);

        return b;
    }
    static B8802 b8802() {
        final B8802 b = new B8802();
        b.setType(B8802.TYPE_AUDIO);
        b.setChannel(10);
        b.setEvent(B8802.EVENT_ALARM_TRIGGERED_BY_ROBBERY);
        b.setStartTime("220616123900");
        b.setEndTime("220616123959");

        return b;
    }
    static B8803 b8803() {
        final B8803 b = new B8803();
        b.setType(B8802.TYPE_AUDIO);
        b.setChannel(10);
        b.setEvent(B8802.EVENT_ALARM_TRIGGERED_BY_ROBBERY);
        b.setStartTime("220616123900");
        b.setEndTime("220616123959");
        b.setDeleted(B8803.DELETED_DELETE);

        return b;
    }
    static B8804 b8804() {
        final B8804 b = new B8804();
        b.setCommand(B8804.COMMAND_SART);
        b.setDuration(10);
        b.setAction(B8804.ACTION_SAVE);
        b.setSampling(B8804.SAMPLING_32K);

        return b;
    }
    static B8805 b8805() {
        final B8805 b = new B8805();
        b.setId(10L);
        b.setDeleted(B8805.DELETED_DELETE);

        return b;
    }
    static B8900 b8900() {
        final B8900 b = new B8900();
        b.setType(B8900.TYPE_RTC_IC_CARD_DATA);
        b.setData(new byte[]{1,2,3,4});

        return b;
    }

    static void fillB0200(B0200 b, long alarmBits) {
        b.setAlarmBits(alarmBits);
        b.setStatusBits(2L);
        b.setLatitude(3L);
        b.setLongitude(4L);
        b.setAltitude(5);
        b.setSpeed(6);
        b.setDirection(7);
        b.setTime("220616123900");
        b.setX01(1L);
        b.setX02(2);
        b.setX03(3);
        b.setX04(4);
        b.setX05(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30});
        b.setX06((short)6);
        b.setX11(b0200X11());
        b.setX12(b0200X12());
        b.setX13(b0200X13());
        b.setX25(25L);
        b.setX2A(0x2A);
        b.setX2B(0x2BL);
        b.setX30(30);
        b.setX31(31);
    }
    static void fillB8103(B8103 b) {
        b.setX0001(1L);
        b.setX0002(2L);
        b.setX0003(3L);
        b.setX0004(4L);
        b.setX0005(5L);
        b.setX0006(6L);
        b.setX0007(7L);
        b.setX0010("x0010");
        b.setX0011("x0011");
        b.setX0012("x0012");
        b.setX0013("x0013");
        b.setX0014("x0014");
        b.setX0015("x0015");
        b.setX0016("x0016");
        b.setX0017("x0017");
        b.setX0018(18L);
        b.setX0019(19L);
        b.setX001A("x001A");
        b.setX001B(0x1BL);
        b.setX001C(0x1CL);
        b.setX001D("x001D");
        b.setX0020(20L);
        b.setX0021(21L);
        b.setX0022(22L);
        b.setX0023("x0023");
        b.setX0024("x0024");
        b.setX0025("x0025");
        b.setX0026("x0026");
        b.setX0027(27L);
        b.setX0028(28L);
        b.setX0029(29L);
        b.setX002C(0x2CL);
        b.setX002D(0x2DL);
        b.setX002E(0x2EL);
        b.setX002F(0x2FL);
        b.setX0030(0x30L);
        b.setX0031(31);
        b.setX0032(new byte[]{1, 2, 3, 4});
        b.setX0040("x0040");
        b.setX0041("x0041");
        b.setX0042("x0042");
        b.setX0043("x0043");
        b.setX0044("x0044");
        b.setX0045(45L);
        b.setX0046(46L);
        b.setX0047(47L);
        b.setX0048("x0048");
        b.setX0049("x0049");
        b.setX0050(0x50L);
        b.setX0051(0x51L);
        b.setX0052(0x52L);
        b.setX0053(0x53L);
        b.setX0054(0x54L);
        b.setX0055(55L);
        b.setX0056(56L);
        b.setX0057(57L);
        b.setX0058(58L);
        b.setX0059(59L);
        b.setX005A(0x5AL);
        b.setX005B(0x5B);
        b.setX005C(0x5C);
        b.setX005D(0x5D);
        b.setX005E(0x5E);
        b.setX0064(64L);
        b.setX0065(65L);
        b.setX0070(70L);
        b.setX0071(71L);
        b.setX0072(72L);
        b.setX0073(73L);
        b.setX0074(74L);
        b.setX0080(80L);
        b.setX0081(81);
        b.setX0082(82);
        b.setX0083("x0083");
        b.setX0084(84);
        b.setX0090(90);
        b.setX0091(91);
        b.setX0092(92);
        b.setX0093(93L);
        b.setX0094(94);
        b.setX0095(95L);
        b.setX0100(100L);
        b.setX0101(101);
        b.setX0102(102L);
        b.setX0103(103);
        b.setX0110(new byte[]{1,2,3,4,5,6,7,8});
    }

}