package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.B8103;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JT/T 消息体编码解码器：0x8103 设置终端参数 // 2019 modify
 *
 * @author togger
 */
public final class B8103Codec implements Codec<B8103> {

    private B8103Codec() {}

    /** 单例 */
    public static final B8103Codec INSTANCE = new B8103Codec();

    @Override
    public void encode(int version, ByteBuf buf, B8103 target) {
        encodeParams(version, buf, target, null);
    }

    @Override
    public void decode(int version, ByteBuf buf, B8103 target) {
        decodeParams(version, buf, target, null);
    }

    @Override
    public B8103 newInstance() {
        return new B8103();
    }

    /**
     * 编码参数列表
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要编码的对象
     * @param others 其他要编码的方法，可以为 {@code null}
     * @param <S> 要编码的对象类型
     */
    public <S extends B8103> void encodeParams(int version, ByteBuf buf, S target, FieldsEncoder<Long, S> others) {
        Codec.writeCountHeadedContent(buf, IntUnit.BYTE, target, (b, v) -> {
            final CountedFieldEncoder<Long> encoder =
                    new CountedLengthHeadedFieldEncoder<>(b, Codec::writeDoubleWord, IntUnit.BYTE);
            PARAMS.values().forEach(f -> f.encode(version, encoder, v));
            if (others != null) {
                others.encode(version, encoder, v);
            }
            return encoder.getCount();
        });
    }

    /**
     * 解码参数列表
     * @param version 版本号
     * @param buf 字节缓冲区
     * @param target 要解码的对象
     * @param other 其他要解码的方法，可以为 {@code null}
     * @param <S> 要解码的对象类型
     */
    public <S extends B8103> void decodeParams(int version, ByteBuf buf, S target, FieldDecoder<Long, S> other) {
        target.setUnknownParams(null);

        int cnt = Codec.readByte(buf);
        while (cnt-- > 0) {
            final long id = Codec.readDoubleWord(buf);
            final ByteBuf fieldBuf = Codec.readSlice(buf, IntUnit.BYTE);
            final FieldCodec<Long, B8103, ?> param = PARAMS.get(id);
            if (param != null) {
                param.decode(version, fieldBuf, target);
            } else if (other == null || !other.decode(id, version, fieldBuf, target)) {
                target.putUnknownParam(id, ByteBufUtil.hexDump(fieldBuf));
            }
        }
    }


    private final static Map<Long, FieldCodec<Long, B8103, ?>> PARAMS = new LinkedHashMap<>();

    private static <V> void register(FieldCodec<Long, B8103, V> fieldCodec) {
        PARAMS.put(fieldCodec.getId(), fieldCodec);
    }

    static {

        // 0x0001 DWORD 终端心跳发送间隔，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0001L, B8103::getX0001, B8103::setX0001));
        // 0x0002 DWORD TCP 消息应答超时时间，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0002L, B8103::getX0002, B8103::setX0002));
        // 0x0003 DWORD TCP 消息重传次数
        register(FieldCodec.ofDoubleWord(0x0003L, B8103::getX0003, B8103::setX0003));
        // 0x0004 DWORD UDP 消息应答超时时间，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0004L, B8103::getX0004, B8103::setX0004));
        // 0x0005 DWORD UDP 消息重传次数
        register(FieldCodec.ofDoubleWord(0x0005L, B8103::getX0005, B8103::setX0005));
        // 0x0006 DWORD SMS 消息应答超时时间，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0006L, B8103::getX0006, B8103::setX0006));
        // 0x0007 DWORD SMS 消息重传次数
        register(FieldCodec.ofDoubleWord(0x0007L, B8103::getX0007, B8103::setX0007));

        // 0x0010 STRING 主服务器 APN，无线通信拨号访问点，若网络制式为 CDMA，则该处为 PPP 拨号号码
        register(FieldCodec.ofString(0x0010L, B8103::getX0010, B8103::setX0010));
        // 0x0011 STRING 主服务器无线通信拨号用户名
        register(FieldCodec.ofString(0x0011L, B8103::getX0011, B8103::setX0011));
        // 0x0012 STRING 主服务器无线通信拨号密码
        register(FieldCodec.ofString(0x0012L, B8103::getX0012, B8103::setX0012));
        // 0x0013 STRING 主服务器地址、IP 或域名(2019 版以冒号分割主机和端口，多个服务器使用分号分隔)
        register(FieldCodec.ofString(0x0013L, B8103::getX0013, B8103::setX0013));
        // 0x0014 STRING 备份服务器 APN，无线通信拨号访问点
        register(FieldCodec.ofString(0x0014L, B8103::getX0014, B8103::setX0014));
        // 0x0015 STRING 备份服务器无线通信拨号用户名
        register(FieldCodec.ofString(0x0015L, B8103::getX0015, B8103::setX0015));
        // 0x0016 STRING 备份服务器无线通信拨号密码
        register(FieldCodec.ofString(0x0016L, B8103::getX0016, B8103::setX0016));
        // 0x0017 STRING 备份服务器地址，IP 或域名(2019 版以冒号分割主机和端口，多个服务器使用分号分隔)
        register(FieldCodec.ofString(0x0017L, B8103::getX0017, B8103::setX0017));
        // 0x0018 DWORD 服务器 TCP 端口 // 2019 del
        register(FieldCodec.of(0x0018L, B8103::getX0018, B8103::setX0018,
                ver -> ver < 1 ? Codec::writeDoubleWord : null,
                ver -> ver < 1 ? Codec::readDoubleWord : null
        ));
        // 0x0019 DWORD 服务器 UDP 端口 // 2019 del
        register(FieldCodec.of(0x0019L, B8103::getX0019, B8103::setX0019,
                ver -> ver < 1 ? Codec::writeDoubleWord : null,
                ver -> ver < 1 ? Codec::readDoubleWord : null
        ));
        // 0x001A STRING 道路运输证 IC 卡认证主服务器 IP 地址或域名
        register(FieldCodec.ofString(0x001AL, B8103::getX001A, B8103::setX001A));
        // 0x001B DWORD 道路运输证 IC 卡认证主服务器 TCP 端口
        register(FieldCodec.ofDoubleWord(0x001BL, B8103::getX001B, B8103::setX001B));
        // 0x001C DWORD 道路运输证 IC 卡认证主服务器 UDP 端口
        register(FieldCodec.ofDoubleWord(0x001CL, B8103::getX001C, B8103::setX001C));
        // 0x001D STRING 道路运输证 IC 卡认证备份服务器 IP 地址或域名，端口同主服务器
        register(FieldCodec.ofString(0x001DL, B8103::getX001D, B8103::setX001D));

        // 0x0020 DWORD 位置汇报策略，0：定时汇报；1：定距汇报；2：定时和定距汇报
        register(FieldCodec.ofDoubleWord(0x0020L, B8103::getX0020, B8103::setX0020));
        // 0x0021 DWORD 位置汇报方案，0：根据 ACC 状态；1：根据登录状态和 ACC 状态，先判断登录状态，若登录再根据 ACC 状态
        register(FieldCodec.ofDoubleWord(0x0021L, B8103::getX0021, B8103::setX0021));
        // 0x0022 DWORD 驾驶员未登录汇报时间间隔，单位为秒（s），值大于 0
        register(FieldCodec.ofDoubleWord(0x0022L, B8103::getX0022, B8103::setX0022));

        // 0x0023 STRING 从服务器 APN，该值为空时，终端应使用主服务器相同配置 // 2019 new
        register(FieldCodec.of(0x0023L, B8103::getX0023, B8103::setX0023,
                ver -> ver > 0 ? Codec::writeString : null,
                ver -> ver > 0 ? Codec::readString : null
        ));
        // 0x0024 STRING 从服务器无线通信拨号用户名，该值为空时，终端应使用主服务器相同配置 // 2019 new
        register(FieldCodec.of(0x0024L, B8103::getX0024, B8103::setX0024,
                ver -> ver > 0 ? Codec::writeString : null,
                ver -> ver > 0 ? Codec::readString : null
        ));
        // 0x0025 STRING 从服务器无线通信拨号密码，该值为空时，终端应使用主服务器相同配置 // 2019 new
        register(FieldCodec.of(0x0025L, B8103::getX0025, B8103::setX0025,
                ver -> ver > 0 ? Codec::writeString : null,
                ver -> ver > 0 ? Codec::readString : null
        ));
        // 0x0026 STRING 从服务器备份地址、IP 地址或域名，主机和端口用冒号分割，多个服务器使用分号分割 // 2019 new
        register(FieldCodec.of(0x0026L, B8103::getX0026, B8103::setX0026,
                ver -> ver > 0 ? Codec::writeString : null,
                ver -> ver > 0 ? Codec::readString : null
        ));

        // 0x0027 DWORD 休眠时汇报时间间隔，单位为秒（s），值大于 0
        register(FieldCodec.ofDoubleWord(0x0027L, B8103::getX0027, B8103::setX0027));
        // 0x0028 DWORD 紧急报警时汇报时间间隔，单位为秒（s），值大于 0
        register(FieldCodec.ofDoubleWord(0x0028L, B8103::getX0028, B8103::setX0028));
        // 0x0029 DWORD 缺省时间汇报间隔，单位为秒（s），值大于 0
        register(FieldCodec.ofDoubleWord(0x0029L, B8103::getX0029, B8103::setX0029));

        // 0x002C DWORD 缺省距离汇报间隔，单位为米（m），值大于 0
        register(FieldCodec.ofDoubleWord(0x002CL, B8103::getX002C, B8103::setX002C));
        // 0x002D DWORD 驾驶员未登录汇报距离间隔，单位为米（m），值大于 0
        register(FieldCodec.ofDoubleWord(0x002DL, B8103::getX002D, B8103::setX002D));
        // 0x002E DWORD 休眠时汇报距离间隔，单位为米（m），值大于 0
        register(FieldCodec.ofDoubleWord(0x002EL, B8103::getX002E, B8103::setX002E));
        // 0x002F DWORD 紧急报警时汇报距离间隔，单位为米（m），值大于 0
        register(FieldCodec.ofDoubleWord(0x002FL, B8103::getX002F, B8103::setX002F));
        // 0x0030 DWORD 拐点补传角度，值小于 180
        register(FieldCodec.ofDoubleWord(0x0030L, B8103::getX0030, B8103::setX0030));
        // 0x0031 WORD 电子围栏半径（非法位移阈值），单位为米（m）
        register(FieldCodec.ofWord(0x0031L, B8103::getX0031, B8103::setX0031));

        // 0x0032 BYTE[4] 违规行驶时段范围，精确到分。BYTE1：违规行驶开始时间的小时部分；BYTE2：违规行驶开始时间的分钟部分；BYTE3：违规行驶结束时间的小时部分；BYTE4：违规行驶结束时间的分钟部分。示例∶0x16320A1E，表示当天晚上10点50分到第二天早上10点30 分属于违规行驶时段 // 2019 new
        register(FieldCodec.of(0x0032L, B8103::getX0032, B8103::setX0032,
                ver -> ver > 0 ? (b, v) -> Codec.writeBytes(b, v, -4, PadChar.NUL) : null,
                ver -> ver > 0 ? b -> Codec.readBytes(b, 4, PadChar.NUL) : null
        ));

        // 0x0040 STRING 监控平台电话号码
        register(FieldCodec.ofString(0x0040L, B8103::getX0040, B8103::setX0040));
        // 0x0041 STRING 复位电话号码，可采用此电话号码拨打终端电话让终端复位
        register(FieldCodec.ofString(0x0041L, B8103::getX0041, B8103::setX0041));
        // 0x0042 STRING 恢复出厂设置电话号码，可采用此电话号码拨打终端电话让终端恢复出厂设置
        register(FieldCodec.ofString(0x0042L, B8103::getX0042, B8103::setX0042));
        // 0x0043 STRING 监控平台 SMS 电话号码
        register(FieldCodec.ofString(0x0043L, B8103::getX0043, B8103::setX0043));
        // 0x0044 STRING 接收终端 SMS 文本报警号码
        register(FieldCodec.ofString(0x0044L, B8103::getX0044, B8103::setX0044));
        // 0x0045 DWORD 终端电话接听策略，0：自动接听；1：ACC ON 时自动接听，OFF 时手动接听
        register(FieldCodec.ofDoubleWord(0x0045L, B8103::getX0045, B8103::setX0045));
        // 0x0046 DWORD 每次最长通话时间，单位为秒（s），0 为不允许通话，0xFFFFFFF 为不限制
        register(FieldCodec.ofDoubleWord(0x0046L, B8103::getX0046, B8103::setX0046));
        // 0x0047 DWORD 当月最长通话时间，单位为秒（s），0 为不允许通话，0xFFFFFFF 为不限制
        register(FieldCodec.ofDoubleWord(0x0047L, B8103::getX0047, B8103::setX0047));
        // 0x0048 STRING 监听电话号码
        register(FieldCodec.ofString(0x0048L, B8103::getX0048, B8103::setX0048));
        // 0x0049 STRING 监管平台特权短信号码
        register(FieldCodec.ofString(0x0049L, B8103::getX0049, B8103::setX0049));

        // 0x0050 DWORD 报警屏蔽字，与位置信息汇报消息中的报警标志相对应，相应位为 1 则相应报警被屏蔽
        register(FieldCodec.ofDoubleWord(0x0050L, B8103::getX0050, B8103::setX0050));
        // 0x0051 DWORD 报警发送文本 SMS开关，与位置信息汇报消息中的报警标志相对应，相应位为 1 则相应报警时发送文本 SMS
        register(FieldCodec.ofDoubleWord(0x0051L, B8103::getX0051, B8103::setX0051));
        // 0x0052 DWORD 报警拍摄开关，与位置信息汇报消息中的报警标志相对应，相应位为 1 则相应报警时摄像头拍摄
        register(FieldCodec.ofDoubleWord(0x0052L, B8103::getX0052, B8103::setX0052));
        // 0x0053 DWORD 报警拍摄存储标志，与位置信息汇报消息中的报警标志相对应，相应位为 1 则对相应报警时拍的照片进行存储，否则实时上传
        register(FieldCodec.ofDoubleWord(0x0053L, B8103::getX0053, B8103::setX0053));
        // 0x0054 DWORD 关键标志，与位置信息汇报消息中的报警标志相对应，相应位为 1 则对相应报警为关键报警
        register(FieldCodec.ofDoubleWord(0x0054L, B8103::getX0054, B8103::setX0054));
        // 0x0055 DWORD 最高速度，单位为千米每小时（km/h）
        register(FieldCodec.ofDoubleWord(0x0055L, B8103::getX0055, B8103::setX0055));
        // 0x0056 DWORD 超速持续时间，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0056L, B8103::getX0056, B8103::setX0056));
        // 0x0057 DWORD 连续驾驶时间门限，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0057L, B8103::getX0057, B8103::setX0057));
        // 0x0058 DWORD 当天累计驾驶时间门限，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0058L, B8103::getX0058, B8103::setX0058));
        // 0x0059 DWORD 最小休息时间，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x0059L, B8103::getX0059, B8103::setX0059));
        // 0x005A DWORD 最长停车时间，单位为秒（s）
        register(FieldCodec.ofDoubleWord(0x005AL, B8103::getX005A, B8103::setX005A));

        // 0x005B WORD 超速预警差值，单位为 1/10 千米每小时（1/10km/h）
        register(FieldCodec.ofWord(0x005BL, B8103::getX005B, B8103::setX005B));
        // 0x005C WORD 疲劳驾驶预警差值，单位为秒（s），值大于 0
        register(FieldCodec.ofWord(0x005CL, B8103::getX005C, B8103::setX005C));
        // 0x005D WORD 碰撞报警参数设置，bit7~bit0 为碰撞时间，单位为毫秒（ms）；bit15~bit8 为碰撞加速度，单位为 0.1g；设置范围为 0~79，默认为 10
        register(FieldCodec.ofWord(0x005DL, B8103::getX005D, B8103::setX005D));
        // 0x005E WORD 侧翻报警参数设置，侧翻角度，单位 1 度，默认为 30 度
        register(FieldCodec.ofWord(0x005EL, B8103::getX005E, B8103::setX005E));

        // 0x0064 DWORD 定时拍照控制，参数项格式和定义见表 14
        register(FieldCodec.ofDoubleWord(0x0064L, B8103::getX0064, B8103::setX0064));
        // 0x0065 DWORD 定距拍照控制，参数项格式和定义见表 15
        register(FieldCodec.ofDoubleWord(0x0065L, B8103::getX0065, B8103::setX0065));

        // 0×0070 DWORD 图像/视频质量，设置范围为 1～10，1 表示最优质量
        register(FieldCodec.ofDoubleWord(0x0070L, B8103::getX0070, B8103::setX0070));
        // 0x0071 DWORD 亮度，设置范围为 0~255
        register(FieldCodec.ofDoubleWord(0x0071L, B8103::getX0071, B8103::setX0071));
        // 0x0072 DWORD 对比度，设置范围为 0～127
        register(FieldCodec.ofDoubleWord(0x0072L, B8103::getX0072, B8103::setX0072));
        // 0x0073 DWORD 饱和度，设置范围为 0~127
        register(FieldCodec.ofDoubleWord(0x0073L, B8103::getX0073, B8103::setX0073));
        // 0x0074 DWORD 色度，设置范围为 0~255
        register(FieldCodec.ofDoubleWord(0x0074L, B8103::getX0074, B8103::setX0074));

        // 0x0080 DWORD 车辆里程表读数，单位：1/10km
        register(FieldCodec.ofDoubleWord(0x0080L, B8103::getX0080, B8103::setX0080));
        // 0x0081 WORD 车辆所在的省域 ID
        register(FieldCodec.ofWord(0x0081L, B8103::getX0081, B8103::setX0081));
        // 0x0082 WORD 车辆所在的市域 ID
        register(FieldCodec.ofWord(0x0082L, B8103::getX0082, B8103::setX0082));
        // 0x0083 STRING 公安交通管理部门颁发的机动车号牌
        register(FieldCodec.ofString(0x0083L, B8103::getX0083, B8103::setX0083));
        // 0x0084 BYTE 车牌颜色，0.未上车牌 1.蓝色 2.黄色 3.黑色 4.白色 5.绿色 9.其他
        register(FieldCodec.ofByte(0x0084L, B8103::getX0084, B8103::setX0084));

        // 0x0090 BYTE GNSS 定位模式，bit0，0：禁用 GPS 定位，1：启用 GPS 定位；bit1，0：禁用北斗定位，1：启用北斗定位；bit2，0：禁用 GLONASS 定位，1：启用 GLONASS 定位；bit3，0：禁用 Galileo 定位，1：启用 Galileo 定位
        register(FieldCodec.ofByte(0x0090L, B8103::getX0090, B8103::setX0090));
        // 0x0091 BYTE GNSS 波特率，0x00：4800；0x01：9600；0x02：19200；0x03：38400；0x04：57600；05：15200
        register(FieldCodec.ofByte(0x0091L, B8103::getX0091, B8103::setX0091));
        // 0x0092 BYTE GNSS 模块详细定位数据输出频率，0x00：500ms；0x01：1000ms（默认值）；0x02：2000ms；0x03：3000ms；Ox04：4000ms
        register(FieldCodec.ofByte(0x0092L, B8103::getX0092, B8103::setX0092));
        // 0x0093 DWORD GNSS 模块详细定位数据采集频率，单位为秒（s），默认为 1
        register(FieldCodec.ofDoubleWord(0x0093L, B8103::getX0093, B8103::setX0093));
        // 0x0094 BYTE GNSS 模块详细定位数据上传方式，0x00：本地存储，不上传（默认值）；0x01：按时间间隔上传；0x02：按距离间隔上传；0xOB：按累计时间上传，达到传输时间后自动停止上传；0xOC：按累计距离上传，达到距离后自动停止上传；0x0D：按累计条数上传，达到上传条数后自动停止上传
        register(FieldCodec.ofByte(0x0094L, B8103::getX0094, B8103::setX0094));
        // 0x0095 DWORD GNSS 模块详细定位数据上传设置，上传方式为 0x01 时，单位为秒（s）；上传方式为 0x02 时，单位为米（m）；上传方式为 0xOB 时，单位为秒（s）；上传方式为 0x0C 时，单位为米（m）；上传方式为 0xOD 时，单位为条
        register(FieldCodec.ofDoubleWord(0x0095L, B8103::getX0095, B8103::setX0095));

        // 0x0100 DWORD CAN 总线通道 1 采集时间间隔，单位为毫秒（ms），0 表示不采集
        register(FieldCodec.ofDoubleWord(0x0100L, B8103::getX0100, B8103::setX0100));
        // 0×0101 WORD CAN 总线通道 1 上传时间间隔，单位为秒（s），0 表示不上传
        register(FieldCodec.ofWord(0x0101L, B8103::getX0101, B8103::setX0101));
        // 0x0102 DWORD CAN 总线通道 2 采集时间间隔，单位为毫秒（ms），0 表示不采集
        register(FieldCodec.ofDoubleWord(0x0102L, B8103::getX0102, B8103::setX0102));
        // 0x0103 WORD CAN 总线通道 2 上传时间间隔，单位为秒（s），0 表示不上传
        register(FieldCodec.ofWord(0x0103L, B8103::getX0103, B8103::setX0103));

        // 0x0110 BYTE[8] CAN 总线 ID 单独采集设置，bit63-bit32 表示此 ID 采集时间间隔（ms），0 表示不采集；bit31 表示 CAN 通道号，0：CAN1，1：CAN2；bit30 表示帧类型，0：标准帧，1：扩展帧；bit29 表示数据采集方式，0：原始数据，1：采集区间的计算值；bit28-bit0 表示 CAN 总线 ID
        register(FieldCodec.of(0x0110L, B8103::getX0110, B8103::setX0110,
                ver -> (b, v) -> Codec.writeBytes(b, v, -8, PadChar.NUL),
                ver -> b -> Codec.readBytes(b, 8, PadChar.NUL)
        ));
    }

}
