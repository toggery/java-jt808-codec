package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.AbstractToStringJoiner;
import io.github.toggery.jt808.messagebody.HexUtil;

import java.util.*;

/**
 * JT/T 消息元数据
 *
 * @author togger
 */
public final class MessageMetadata extends AbstractToStringJoiner {

    /**
     * 获取一个默认的入站消息元数据字典
     * @return 默认的入站消息元数据字典
     */
    public static Map<Integer, MessageMetadata> inbounds() {
        return inbounds(null);
    }

    /**
     * 获取一个定制的入站消息元数据字典
     * @param customizedInbounds 定制的入站消息元数据字典，替换或添加到默认入站消息元数据字典中，可以为 {@code null}
     * @return 定制的入站消息元数据字典
     */
    public static Map<Integer, MessageMetadata> inbounds(MessageMetadata[] customizedInbounds) {
        return Collections.unmodifiableMap(map(getDefaultInbounds(), customizedInbounds));
    }

    /**
     * 获取一个默认的出站消息元数据字典
     * @return 默认的出站消息元数据字典
     */
    public static Map<Integer, MessageMetadata> outbounds() {
        return outbounds(null);
    }

    /**
     * 获取一个定制的出站消息元数据字典
     * @param customizedOutbounds 定制的出站消息元数据字典，替换或添加到默认出站消息元数据字典中，可以为 {@code null}
     * @return 定制的出站消息元数据字典
     */
    public static Map<Integer, MessageMetadata> outbounds(MessageMetadata[] customizedOutbounds) {
        return Collections.unmodifiableMap(map(getDefaultOutbounds(), customizedOutbounds));
    }


    /**
     * 实例化一个 {@link MessageMetadata}
     * @param id 消息 ID
     * @param name 消息名称
     * @param bodyCodec 消息体编码解码器
     * @throws NullPointerException 如果任何一个参数对象为 {@code null}
     */
    public MessageMetadata(int id, String name, Codec<?> bodyCodec) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.bodyCodec = Objects.requireNonNull(bodyCodec);
    }

    /**
     * 实例化一个 {@link MessageMetadata}
     * @param id 消息 ID
     * @param name 消息名称
     * @throws NullPointerException 如果参数对象 {@code name} 为 {@code null}
     */
    public MessageMetadata(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.bodyCodec = null;
    }


    /**
     * 获取消息 ID
     * @return 消息 ID
     */
    public int getId() {
        return id;
    }

    /**
     * 获取消息名称
     * @return 消息名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取消息体编码解码器
     * @return 消息体编码解码器，可能为 {@code null} 表示无消息体
     */
    public Codec<?> getBodyCodec() {
        return bodyCodec;
    }


    @Override
    protected void toStringJoiner(StringJoiner joiner) {
        joiner
                .add(HexUtil.wordString("id=", id))
                .add("name=" + name)
                .add("bodyCodec=" + (bodyCodec == null ? "" : bodyCodec.getClass().getName()))
        ;
    }

    private final int id;
    private final String name;
    private final Codec<?> bodyCodec;


    private static MessageMetadata[] getDefaultInbounds() {
        // 87 / 3 = 29
        return new MessageMetadata[]{

                // 0x0001 终端通用应答
                new MessageMetadata(0x0001, "0x0001 终端通用应答", B0001Codec.INSTANCE),

                // 0x0002 终端心跳【消息体为空】 <- 0x8001
                new MessageMetadata(0x0002, "0x0002 终端心跳"),

                // 0x0003 终端注销【消息体为空】 <- 0x8001
                new MessageMetadata(0x0003, "0x0003 终端注销"),

                // 0x0004 查询服务器时间请求【消息体为空】 <- 0x8004 // 2019 new
                new MessageMetadata(0x0004, "0x0004 查询服务器时间"),

                // 0x0005 终端补传分包请求 // 2019 new
                new MessageMetadata(0x0005, "0x0005 终端补传分包请求", B0005Codec.INSTANCE),

                // 0x0100 终端注册 <- 0x8100 // 2019 modify
                new MessageMetadata(0x0100, "0x0100 终端注册", B0100Codec.INSTANCE),

                // 0x0102 终端鉴权 <- 0x8001 // 2019 modify
                new MessageMetadata(0x0102, "0x0102 终端鉴权", B0102Codec.INSTANCE),

                // 0x0104 查询终端参数应答 -> 0x8104,  0x8106
                new MessageMetadata(0x0104, "0x0104 查询终端参数应答", B0104Codec.INSTANCE),

                // 0x0107 查询终端属性应答 -> 0x8107 // 2019 modify
                new MessageMetadata(0x0107, "0x0107 查询终端属性应答", B0107Codec.INSTANCE),

                // 0x0108 终端升级结果通知 <- 0x8001
                new MessageMetadata(0x0108, "0x0108 终端升级结果通知", B0108Codec.INSTANCE),

                // 0x0200 位置信息汇报 <- 0x8001 // 2019 modify
                new MessageMetadata(0x0200, "0x0200 位置信息汇报", B0200Codec.INSTANCE),

                // 0x0201 位置信息查询应答 -> 0x8201
                new MessageMetadata(0x0201, "0x0201 位置信息查询应答", B0201Codec.INSTANCE),

                // 0x0301 事件报告 <- 0x8001 // 2019 del
                new MessageMetadata(0x0301, "0x0301 事件报告", B0301Codec.INSTANCE),

                // 0x0302 提问应答 <- 0x8001 // 2019 del
                new MessageMetadata(0x0302, "0x0302 提问应答", B0302Codec.INSTANCE),

                // 0x0303 信息点播/取消 <- 0x8001 // 2019 del
                new MessageMetadata(0x0303, "0x0303 信息点播/取消", B0303Codec.INSTANCE),

                // 0x0500 车辆控制应答 <- 0x8001
                new MessageMetadata(0x0500, "0x0500 车辆控制应答", B0500Codec.INSTANCE),

                // 0x0608 查询区域或线路数据应答 -> 0x8608 // 2019 new
                new MessageMetadata(0x0608, "0x0608 查询区域或线路数据应答", B0608Codec.INSTANCE),

                // 0x0700 行驶记录数据上传（应答） -> 0x8700
                new MessageMetadata(0x0700, "0x0700 行驶记录数据上传（应答）", B0700Codec.INSTANCE),

                // 0x0701 电子运单上报 <- 0x8001
                new MessageMetadata(0x0701, "0x0701 电子运单上报", B0701Codec.INSTANCE),

                // 0x0702 驾驶员身份信息采集上报 <- 0x8001 // 2019 modify
                new MessageMetadata(0x0702, "0x0702 驾驶员身份信息采集上报", B0702Codec.INSTANCE),

                // 0x0704 定位数据批量上传 <- 0x8001
                new MessageMetadata(0x0704, "0x0704 定位数据批量上传", B0704Codec.INSTANCE),

                // 0x0705 CAN 总线数据上传 <- 0x8001 // 2019 modify
                new MessageMetadata(0x0705, "0x0705 CAN 总线数据上传", B0705Codec.INSTANCE),

                // 0x0800 多媒体事件信息上传 <- 0x8001
                new MessageMetadata(0x0800, "0x0800 多媒体事件信息上传", B0800Codec.INSTANCE),

                // 0x0801 多媒体数据上传 <- 0x8800 // 2019 modify
                new MessageMetadata(0x0801, "0x0801 多媒体数据上传", B0801Codec.INSTANCE),

                // 0x0802 存储多媒体数据检索应答 -> 0x8802
                new MessageMetadata(0x0802, "0x0802 存储多媒体数据检索应答", B0802Codec.INSTANCE),

                // 0x0805 摄像头立即拍摄命令应答 <- 0x8001
                new MessageMetadata(0x0805, "0x0805 摄像头立即拍摄命令应答", B0805Codec.INSTANCE),

                // 0x0900 数据上行透传 <- 0x8001
                new MessageMetadata(0x0900, "0x0900 数据上行透传", B0900Codec.INSTANCE),

                // 0x0901 数据压缩上报 <- 0x8001
                new MessageMetadata(0x0901, "0x0901 数据压缩上报", B0901Codec.INSTANCE),

                // 0x0A00 终端 RSA 公钥 <- 0x8001
                new MessageMetadata(0x0A00, "0x0A00 终端 RSA 公钥", B0A00Codec.INSTANCE),

        };
    }

    private static MessageMetadata[] getDefaultOutbounds() {
        // 126 / 3 = 42
        return new MessageMetadata[]{

                // 0x8001 平台通用应答
                new MessageMetadata(0x8001, "0x8001 平台通用应答", B8001Codec.INSTANCE),

                // 0x8003 服务器补传分包请求
                new MessageMetadata(0x8003, "0x8003 服务器补传分包请求", B8003Codec.INSTANCE),

                // 0x8004 查询服务器时间应答 -> 0x0004 // 2019 new
                new MessageMetadata(0x8004, "0x8004 查询服务器时间应答", B8004Codec.INSTANCE),

                // 0x8100 终端注册应答 -> 0x0100
                new MessageMetadata(0x8100, "0x8100 终端注册应答", B8100Codec.INSTANCE),

                // 0x8103 设置终端参数 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8103, "0x8103 设置终端参数", B8103Codec.INSTANCE),

                // 0x8104 查询终端参数【消息体为空】 <- 0x0104
                new MessageMetadata(0x8104, "0x8104 查询终端参数"),

                // 0x8105 终端控制 <- 0x0001
                new MessageMetadata(0x8105, "0x8105 终端控制", B8105Codec.INSTANCE),

                // 0x8106 查询指定终端参数 <- 0x0104
                new MessageMetadata(0x8106, "0x8106 查询指定终端参数", B8106Codec.INSTANCE),

                // 0x8107 查询终端属性【消息体为空】 <- 0x0107
                new MessageMetadata(0x8107, "0x8107 查询终端属性"),

                // 0x8108 下发终端升级包 <- 0x0001
                new MessageMetadata(0x8108, "0x8108 下发终端升级包", B8108Codec.INSTANCE),

                // 0x8201 位置信息查询【消息体为空】 <- 0x0201
                new MessageMetadata(0x8201, "0x8201 位置信息查询"),

                // 0x8202 临时位置跟踪控制 <- 0x0001
                new MessageMetadata(0x8202, "0x8202 临时位置跟踪控制", B8202Codec.INSTANCE),

                // 0x8203 人工确认报警消息 <- 0x0001
                new MessageMetadata(0x8203, "0x8203 人工确认报警消息", B8203Codec.INSTANCE),

                // 0x8204 链路检测请求【消息体为空】 <- 0x0001 // 2019 new
                new MessageMetadata(0x8204, "0x8204 链路检测"),

                // 0x8300 文本信息下发 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8300, "0x8300 文本信息下发", B8300Codec.INSTANCE),

                // 0x8301 事件设置 <- 0x0001 // 2019 del
                new MessageMetadata(0x8301, "0x8301 事件设置", B8301Codec.INSTANCE),

                // 0x8302 提问下发 <- 0x0001 // 2019 del
                new MessageMetadata(0x8302, "0x8302 提问下发", B8302Codec.INSTANCE),

                // 0x8303 信息点播菜单设置 <- 0x0001 // 2019 del
                new MessageMetadata(0x8303, "0x8303 信息点播菜单设置", B8303Codec.INSTANCE),

                // 0x8304 信息服务 <- 0x0001 // 2019 del
                new MessageMetadata(0x8304, "0x8304 信息服务", B8304Codec.INSTANCE),

                // 0x8400 电话回拨 <- 0x0001
                new MessageMetadata(0x8400, "0x8400 电话回拨", B8400Codec.INSTANCE),

                // 0x8401 设置电话本 <- 0x0001
                new MessageMetadata(0x8401, "0x8401 设置电话本", B8401Codec.INSTANCE),

                // 0x8500 车辆控制 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8500, "0x8500 车辆控制", B8500Codec.INSTANCE),

                // 0x8600 设置圆形区域 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8600, "0x8600 设置圆形区域", B8600Codec.INSTANCE),

                // 0x8601 删除圆形区域 <- 0x0001
                new MessageMetadata(0x8601, "0x8601 删除圆形区域", B8601Codec.INSTANCE),

                // 0x8602 设置矩形区域 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8602, "0x8602 设置矩形区域", B8602Codec.INSTANCE),

                // 0x8603 删除矩形区域 <- 0x0001
                new MessageMetadata(0x8603, "0x8603 删除矩形区域", B8603Codec.INSTANCE),

                // 0x8604 设置多边形区域 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8604, "0x8604 设置多边形区域", B8604Codec.INSTANCE),

                // 0x8605 删除多边形区域 <- 0x0001
                new MessageMetadata(0x8605, "0x8605 删除多边形区域", B8605Codec.INSTANCE),

                // 0x8606 设置路线 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8606, "0x8606 设置路线", B8606Codec.INSTANCE),

                // 0x8607 删除路线 <- 0x0001
                new MessageMetadata(0x8607, "0x8607 删除路线", B8607Codec.INSTANCE),

                // 0x8608 查询区域或线路数据 <- 0x0608 // 2019 new
                new MessageMetadata(0x8608, "0x8608 查询区域或线路数据", B8608Codec.INSTANCE),

                // 0x8700 行驶记录数据采集命令 <- 0x0700 // 2013 modify
                new MessageMetadata(0x8700, "0x8700 行驶记录数据采集命令", B8700Codec.INSTANCE),

                // 0x8701 行驶记录参数下传命令 <- 0x0001
                new MessageMetadata(0x8701, "0x8701 行驶记录参数下传命令", B8701Codec.INSTANCE),

                // 0x8702 上报驾驶员身份信息请求【消息体为空】 <- 0x0001
                new MessageMetadata(0x8702, "0x8702 上报驾驶员身份信息请求"),

                // 0x8800 多媒体数据上传应答 -> 0x0801
                new MessageMetadata(0x8800, "0x8800 多媒体数据上传应答", B8800Codec.INSTANCE),

                // 0x8801 摄像头立即拍摄命令 <- 0x0001 // 2019 modify
                new MessageMetadata(0x8801, "0x8801 摄像头立即拍摄命令", B8801Codec.INSTANCE),

                // 0x8802 存储多媒体数据检索 <- 0x0802
                new MessageMetadata(0x8802, "0x8802 存储多媒体数据检索", B8802Codec.INSTANCE),

                // 0x8803 存储多媒体数据上传 <- 0x0001
                new MessageMetadata(0x8803, "0x8803 存储多媒体数据上传", B8803Codec.INSTANCE),

                // 0x8804 录音开始命令 <- 0x0001
                new MessageMetadata(0x8804, "0x8804 录音开始命令", B8804Codec.INSTANCE),

                // 0x8805 单条存储多媒体数据检索上传命令 <- 0x0001
                new MessageMetadata(0x8805, "0x8805 单条存储多媒体数据检索上传命令", B8805Codec.INSTANCE),

                // 0x8900 数据下行透传 <- 0x0001
                new MessageMetadata(0x8900, "0x8900 数据下行透传", B8900Codec.INSTANCE),

                // 0x8A00 平台 RSA 公钥 <- 0x0A00
                new MessageMetadata(0x8A00, "0x8A00 平台 RSA 公钥", B8A00Codec.INSTANCE),

        };
    }

    private static Map<Integer, MessageMetadata> map(MessageMetadata[] first, MessageMetadata[] second) {
        final LinkedHashMap<Integer, MessageMetadata> result = new LinkedHashMap<>();
        putAll(result, first);
        putAll(result, second);
        return result;
    }

    private static void putAll(Map<Integer, MessageMetadata> map, MessageMetadata[] arr) {
        if (arr == null || arr.length <= 0) return;

        Arrays.stream(arr)
                .sorted(Comparator.comparing(MessageMetadata::getId))
                .forEach(m -> map.put(m.id, m));
    }

}
