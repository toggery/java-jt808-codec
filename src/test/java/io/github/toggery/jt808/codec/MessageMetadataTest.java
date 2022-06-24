package io.github.toggery.jt808.codec;

import io.github.toggery.jt808.messagebody.HexUtil;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author togger
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageMetadataTest {

    @Test
    @Order(1)
    @DisplayName("1.打印【默认】的【入站】消息元数据集合")
    void inbounds() {
        print("【默认入站】", MessageMetadata.inbounds().values());
    }

    @Test
    @Order(2)
    @DisplayName("2.打印【默认】的【出站】消息元数据集合")
    void outbounds() {
        print("【默认出站】", MessageMetadata.outbounds().values());
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


    private static void print(String name, Collection<MessageMetadata> metadataCollection) {
        System.out.printf("%s消息元数据集合: 共计 %d 项（按照消息 ID 递增自动顺序）", name, metadataCollection.size());
        System.out.println();
        int index = 0;
        for (final MessageMetadata metadata : metadataCollection) {
            System.out.printf("%d/%d. ", ++index, metadataCollection.size());
            System.out.println(metadata);
            final String id = HexUtil.wordString(metadata.getId()).substring(2).toUpperCase();
            final boolean sameName = Optional.ofNullable(metadata.getName())
                    .map(String::toUpperCase)
                    .map(n -> n.toUpperCase().indexOf(id) > 0)
                    .orElse(false);
            final boolean sameBody = Optional.ofNullable(metadata.getBodyCodec())
                    .map(Object::getClass).map(Class::getSimpleName).map(String::toUpperCase)
                    .map(n -> n.indexOf(id) > 0)
                    .orElse(true);
            assertTrue(sameName && sameBody, "消息 ID、消息名称与消息体编码解码器命名应符合统一规则");
        }
    }

}