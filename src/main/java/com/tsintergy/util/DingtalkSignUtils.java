package com.tsintergy.util;



import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 钉钉签名生成工具类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/25 14:24
 */
public class DingtalkSignUtils {

    /**
     * 获取当前时间戳
     * @return 当前时间戳
     */
    public static long getTimestamp(){
        return System.currentTimeMillis();
    }

    /**
     * 获取Dingtalk签名
     * @param timestamp 当前时间戳
     * @param secret 密钥，从Dingtalk群中群机器人配置中的获取
     * @return Dingtalk签名
     * @throws Exception
     */
    public static String getSign(long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
    }
}
