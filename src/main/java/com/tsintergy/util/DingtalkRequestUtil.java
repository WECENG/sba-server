package com.tsintergy.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.message.DingtalkMessage;
import com.tsintergy.message.DingtalkMessageBuilder;
import com.tsintergy.message.DingtalkTextMessage;
import com.tsintergy.message.JvmMemoryInfo;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static cn.hutool.core.date.format.FastDateFormat.MEDIUM;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * <p>
 * 钉钉消息请求工具类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 16:40
 */
@Slf4j
public class DingtalkRequestUtil {

    /**
     * 构建钉钉url
     *
     * @return 钉钉url
     */
    public static String buildDingtalkUrl(DingtalkProperties dingtalkProperties) throws Exception {
        StringBuilder dingtalkUrlBuilder = new StringBuilder(dingtalkProperties.getUrl());
        Assert.notNull(dingtalkProperties.getToken(), "请配置钉钉消息通知token!");
        if (StringUtils.isNotBlank(dingtalkProperties.getToken())) {
            dingtalkUrlBuilder.append("?");
            dingtalkUrlBuilder.append("access_token=");
            dingtalkUrlBuilder.append(dingtalkProperties.getToken());
        }
        if (StringUtils.isNotBlank(dingtalkProperties.getSecret())) {
            long timestamp = DingtalkSignUtils.getTimestamp();
            String sign = DingtalkSignUtils.getSign(timestamp, dingtalkProperties.getSecret());
            dingtalkUrlBuilder.append("&");
            dingtalkUrlBuilder.append("timestamp=");
            dingtalkUrlBuilder.append(timestamp);
            dingtalkUrlBuilder.append("&");
            dingtalkUrlBuilder.append("sign=");
            dingtalkUrlBuilder.append(sign);
        }
        return dingtalkUrlBuilder.toString();
    }

    /**
     * 构造下线消息内容
     *
     * @param instance 实例
     * @return 消息内容
     */
    public static String buildDownOrOfflineContent(Instance instance) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(instance.getRegistration().getName());
        contentBuilder.append("已下线");
        contentBuilder.append("\n");
        contentBuilder.append("应用地址：");
        contentBuilder.append(instance.getRegistration().getServiceUrl());
        contentBuilder.append("\n");
        contentBuilder.append("下线时间：");
        Date downDate = Date.from(instance.getStatusTimestamp());
        contentBuilder.append(FastDateFormat.getDateTimeInstance(MEDIUM, MEDIUM).format(downDate));
        contentBuilder.append("\n");
        contentBuilder.append("当前时间：");
        Date nowDate = new Date();
        contentBuilder.append(FastDateFormat.getDateTimeInstance(MEDIUM, MEDIUM).format(nowDate));
        contentBuilder.append("\n");
        contentBuilder.append("持续时间：");
        contentBuilder.append(DateUtil.between(nowDate, downDate, DateUnit.MINUTE, true));
        contentBuilder.append("分钟");
        contentBuilder.append("\n");
        contentBuilder.append("详情：");
        contentBuilder.append(JSON.toJSONString(instance));
        return contentBuilder.toString();
    }

    /**
     * 构造重新上线消息内容
     *
     * @param instance 实例
     * @return 消息内容
     */
    public static String buildDownToUpContent(Instance instance) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(instance.getRegistration().getName());
        contentBuilder.append("已重新上线");
        contentBuilder.append("\n");
        contentBuilder.append("应用地址：");
        contentBuilder.append(instance.getRegistration().getServiceUrl());
        contentBuilder.append("\n");
        contentBuilder.append("上线时间：");
        Date nowDate = new Date();
        contentBuilder.append(FastDateFormat.getDateTimeInstance(MEDIUM, MEDIUM).format(nowDate));
        contentBuilder.append("\n");
        contentBuilder.append("详情：");
        contentBuilder.append(JSON.toJSONString(instance));
        return contentBuilder.toString();
    }

    /**
     * 构造jvm消息内容
     *
     * @param instance      实例对象
     * @param jvmMemoryInfo jvm信息
     * @return jvm消息内容
     */
    public static String buildJvmContent(Instance instance, JvmMemoryInfo jvmMemoryInfo) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(instance.getRegistration().getName());
        contentBuilder.append("JVM堆或非堆内存告警");
        contentBuilder.append("\n");
        contentBuilder.append("应用地址：");
        contentBuilder.append(instance.getRegistration().getServiceUrl());
        contentBuilder.append("\n");
        contentBuilder.append("已使用堆内存：");
        contentBuilder.append(jvmMemoryInfo.getUsedHeap());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("已分配堆内存：");
        contentBuilder.append(jvmMemoryInfo.getCommittedHeap());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("最大堆内存：");
        contentBuilder.append(jvmMemoryInfo.getMaxHeap());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("已使用非堆内存：");
        contentBuilder.append(jvmMemoryInfo.getUsedNonHeap());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("最大非堆内存：");
        contentBuilder.append(jvmMemoryInfo.getMaxNonHeap());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("剩余可用内存：");
        contentBuilder.append(jvmMemoryInfo.getSpareHead());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("剩余可分配堆内存：");
        contentBuilder.append(jvmMemoryInfo.getSpareCommitHead());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("剩余最大可用堆内存：");
        contentBuilder.append(jvmMemoryInfo.getSpareMaxHead());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        contentBuilder.append("剩余可用非堆内存：");
        contentBuilder.append(jvmMemoryInfo.getSpareNonHeap());
        contentBuilder.append("M");
        contentBuilder.append("\n");
        return contentBuilder.toString();
    }

    /**
     * 发送钉钉消息
     *
     * @param restTemplate       rest对象
     * @param dingtalkProperties 属性对象
     * @param content            文本内容
     * @throws Exception
     */
    public static void sendDingTalkMes(RestTemplate restTemplate, DingtalkProperties dingtalkProperties, String content) {
        try {
            String dingtalkUrl = buildDingtalkUrl(dingtalkProperties);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            DingtalkTextMessage textMessage = DingtalkTextMessage.builder()
                    .content(content)
                    .build();
            DingtalkMessage dingtalkMessage = DingtalkMessageBuilder.textMessageBuilder()
                    .textMessage(textMessage)
                    .build();
            HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(dingtalkMessage), headers);
            restTemplate.postForObject(dingtalkUrl, requestEntity, Object.class);
        } catch (Exception e) {
            log.error("钉钉消息发送异常:", e);
        }

    }

}
