package com.tsintergy.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("单元测试-Dingtalk签名")
class DingtalkSignUtilsTest {

    @Test
    @DisplayName("获取当前时间戳")
    void getTimestamp() {
        assertDoesNotThrow(DingtalkSignUtils::getTimestamp);
    }

    @Test
    @DisplayName("获取Dingtalk签名")
    void getSign() throws Exception {
        String secret = "SEC6816d5f8c552a52f6deb9d5d0e2faac0c9030bb6d3515efeeee4845c35a4c07b";
        long timestamp = DingtalkSignUtils.getTimestamp();
        assertDoesNotThrow(() -> DingtalkSignUtils.getSign(timestamp, secret));
        String sign = DingtalkSignUtils.getSign(timestamp, secret);
        System.out.println("当前时间戳：" + timestamp);
        System.out.println("Dingtalk签名：" + sign);
    }
}