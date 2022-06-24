package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.*;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * JT/T 消息体编码解码器：0x0608 查询区域或线路数据应答 // 2019 new
 *<br><br>
 * ？？？矛盾：协议中集合元素类型为 DWORD，而描述则是区域或路线消息体数据格式 ？？？
 *
 * @author togger
 */
public final class B0608Codec implements Codec<B0608> {

    private B0608Codec() {}

    /** 单例 */
    public static final B0608Codec INSTANCE = new B0608Codec();

    @Override
    public void encode(int version, ByteBuf buf, B0608 target) {
        Codec.writeByte(buf, target.getType());

        switch (target.getType()) {
            case 1:
                Codec.writeCountHeadedContent(buf, IntUnit.DWORD, target.getCircles(), (b, that) -> {
                    int count = 0;
                    for (final B8600.Region region : that) {
                        B8600Codec.RegionCodec.INSTANCE.encode(version, b, region);
                        count++;
                    }
                    return count;
                });
                break;
            case 2:
                Codec.writeCountHeadedContent(buf, IntUnit.DWORD, target.getRectangles(), (b, that) -> {
                    int count = 0;
                    for (final B8602.Region region : that) {
                        B8602Codec.RegionCodec.INSTANCE.encode(version, b, region);
                        count++;
                    }
                    return count;
                });
                break;
            case 3:
                Codec.writeCountHeadedContent(buf, IntUnit.DWORD, target.getPolygons(), (b, that) -> {
                    int count = 0;
                    for (final B8604 polygon : that) {
                        B8604Codec.INSTANCE.encode(version, b, polygon);
                        count++;
                    }
                    return count;
                });
                break;
            case 4:
                Codec.writeCountHeadedContent(buf, IntUnit.DWORD, target.getRoutes(), (b, that) -> {
                    int count = 0;
                    for (final B8606 route : that) {
                        B8606Codec.INSTANCE.encode(version, b, route);
                        count++;
                    }
                    return count;
                });
                break;
            default:
                throw new IllegalArgumentException("Unhandled: type=" + target.getType());
        }
    }

    @Override
    public void decode(int version, ByteBuf buf, B0608 target) {
        target.setType(Codec.readByte(buf));

        Runnable task;
        switch (target.getType()) {
            case B0608.TYPE_CIRCLE:
                final List<B8600.Region> circles = target.getCircles();
                circles.clear();
                task = () -> circles.add(B8600Codec.RegionCodec.INSTANCE.decode(version, buf));
                break;
            case B0608.TYPE_RECTANGLE:
                final List<B8602.Region> rectangles = target.getRectangles();
                rectangles.clear();
                task = () -> rectangles.add(B8602Codec.RegionCodec.INSTANCE.decode(version, buf));
                break;
            case B0608.TYPE_POLYGON:
                final List<B8604> polygons = target.getPolygons();
                polygons.clear();
                task = () -> polygons.add(B8604Codec.INSTANCE.decode(version, buf));
                break;
            case B0608.TYPE_ROUTE:
                final List<B8606> routes = target.getRoutes();
                routes.clear();
                task = () -> routes.add(B8606Codec.INSTANCE.decode(version, buf));
                break;
            default:
                throw new IllegalArgumentException("Unhandled: type=" + target.getType());
        }

        long cnt = Codec.readDoubleWord(buf);
        while (cnt-- > 0) {
            task.run();
        }
    }

    @Override
    public B0608 newInstance() {
        return new B0608();
    }

}
