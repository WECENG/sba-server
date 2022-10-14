package com.tsintergy.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.message.*;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Date;

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
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认标题
     */
    public static final String DEFAULT_TITLE = "应用告警";

    /**
     * 换行
     */
    public static final String LINE_FEED = "  \n";

    /**
     * 引用
     */
    public static final String QUOTE = ">";

    /**
     * 换行 + 引用
     */
    public static final String LINE_FEED_QUOTE = LINE_FEED + QUOTE;

    /**
     * 详情模版,markdown不支持
     */
    public static final String DETAIL_TEMPLATE =
            "<details>\n" +
                    "<summary>{0}</summary>\n" +
                    "{1}\n" +
                    "</details>";

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
        Date downDate = Date.from(instance.getStatusTimestamp());
        Date nowDate = new Date();
        return "## " +
                instance.getRegistration().getName() +
                "已下线" +
                LINE_FEED_QUOTE +
                "应用地址：" +
                instance.getRegistration().getServiceUrl() +
                LINE_FEED_QUOTE +
                "下线时间：" +
                FastDateFormat.getInstance(DATE_FORMAT).format(downDate) +
                LINE_FEED_QUOTE +
                "当前时间：" +
                FastDateFormat.getInstance(DATE_FORMAT).format(nowDate) +
                LINE_FEED_QUOTE +
                "持续时间：" +
                DateUtil.between(nowDate, downDate, DateUnit.MINUTE, true) +
                "分钟" +
                LINE_FEED_QUOTE +
                MessageFormat.format(DETAIL_TEMPLATE, "详情信息", JSON.toJSONString(instance));
    }

    /**
     * 构造重新上线消息内容
     *
     * @param instance 实例
     * @return 消息内容
     */
    public static String buildDownToUpContent(Instance instance) {
        Date nowDate = new Date();
        return "## " +
                instance.getRegistration().getName() +
                "已重新上线" +
                LINE_FEED_QUOTE +
                "应用地址：" +
                instance.getRegistration().getServiceUrl() +
                LINE_FEED_QUOTE +
                "上线时间：" +
                FastDateFormat.getInstance(DATE_FORMAT).format(nowDate) +
                LINE_FEED_QUOTE +
                MessageFormat.format(DETAIL_TEMPLATE, "详情信息", JSON.toJSONString(instance));
    }

    /**
     * 构造jvm消息内容
     *
     * @param instance      实例对象
     * @param jvmMemoryInfo jvm信息
     * @return jvm消息内容
     */
    public static String buildJvmContent(Instance instance, JvmMemoryInfo jvmMemoryInfo) {
        return "## " +
                instance.getRegistration().getName() +
                "内存告警（" +
                jvmMemoryInfo.getWarningTitle() +
                "）" +
                LINE_FEED_QUOTE +
                "应用地址：" +
                instance.getRegistration().getServiceUrl() +
                LINE_FEED_QUOTE +
                "已使用堆内存：" +
                jvmMemoryInfo.getUsedHeap() +
                "M" +
                LINE_FEED_QUOTE +
                "已分配堆内存：" +
                jvmMemoryInfo.getCommittedHeap() +
                "M" +
                LINE_FEED_QUOTE +
                "最大堆内存：" +
                jvmMemoryInfo.getMaxHeap() +
                "M" +
                LINE_FEED_QUOTE +
                "已使用非堆内存：" +
                jvmMemoryInfo.getUsedNonHeap() +
                "M" +
                LINE_FEED_QUOTE +
                "最大非堆内存：" +
                jvmMemoryInfo.getMaxNonHeap() +
                "M" +
                LINE_FEED_QUOTE +
                "剩余可用内存：" +
                jvmMemoryInfo.getSpareHead() +
                "M" +
                LINE_FEED_QUOTE +
                "剩余可分配堆内存：" +
                jvmMemoryInfo.getSpareCommitHead() +
                "M" +
                LINE_FEED_QUOTE +
                "剩余最大可用堆内存：" +
                jvmMemoryInfo.getSpareMaxHead() +
                "M" +
                LINE_FEED_QUOTE +
                "剩余可用非堆内存：" +
                jvmMemoryInfo.getSpareNonHeap() +
                "M" +
                LINE_FEED_QUOTE;
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
        sendDingTalkMes(restTemplate, dingtalkProperties, DEFAULT_TITLE, content);
    }

    /**
     * 发送钉钉消息
     *
     * @param restTemplate       rest对象
     * @param dingtalkProperties 属性对象
     * @param title              标题
     * @param content            内容
     */
    public static void sendDingTalkMes(RestTemplate restTemplate, DingtalkProperties dingtalkProperties, String title, String content) {
        try {
            String dingtalkUrl = buildDingtalkUrl(dingtalkProperties);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            DingtalkMdMessage mdMessage = DingtalkMdMessage.builder()
                    .title(title)
                    .content(content)
                    .build();
            HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(mdMessage), headers);
            restTemplate.postForObject(dingtalkUrl, requestEntity, Object.class);
        } catch (Exception e) {
            log.error("钉钉消息发送异常:", e);
        }
    }

}
