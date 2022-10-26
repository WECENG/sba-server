package com.tsintergy.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.message.CpuInfo;
import com.tsintergy.message.DingtalkMdMessage;
import com.tsintergy.message.JvmMemoryInfo;
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
    public static final String DEFAULT_TITLE_TEMPLATE = "{0}应用告警";

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
     * 客户端钉钉token
     */
    public static final String CLIENT_DING_TALK_TOKEN_KEY = "dt-token";

    /**
     * 客户端钉钉密钥
     */
    public static final String CLIENT_DING_TALK_SECRET_KEY = "dt-secret";

    /**
     * 应用名称
     */
    public static final String APP_NAME_KEY = "app-name";


    /**
     * 获取客户端钉钉token
     *
     * @param instance 实例对象
     * @return 客户端钉钉token
     */
    public static String getClientDingtalkToken(Instance instance, DingtalkProperties dingtalkProperties) {
        return instance.getRegistration().getMetadata().getOrDefault(CLIENT_DING_TALK_TOKEN_KEY, dingtalkProperties.getToken());
    }

    /**
     * 获取客户端钉钉密钥
     *
     * @param instance 实例对象
     * @return 客户端钉钉密钥
     */
    public static String getClientDingtalkSecret(Instance instance, DingtalkProperties dingtalkProperties) {
        return instance.getRegistration().getMetadata().getOrDefault(CLIENT_DING_TALK_SECRET_KEY, dingtalkProperties.getSecret());
    }

    /**
     * 构建钉钉url
     *
     * @return 钉钉url
     */
    public static String buildDingtalkUrl(DingtalkProperties dingtalkProperties, Instance instance) throws Exception {
        StringBuilder dingtalkUrlBuilder = new StringBuilder(dingtalkProperties.getUrl());
        Assert.notNull(dingtalkProperties.getToken(), "请配置钉钉消息通知token!");
        String token = getClientDingtalkToken(instance, dingtalkProperties);
        String secret = getClientDingtalkSecret(instance, dingtalkProperties);
        if (StringUtils.isNotBlank(token)) {
            dingtalkUrlBuilder.append("?");
            dingtalkUrlBuilder.append("access_token=");
            dingtalkUrlBuilder.append(token);
        }
        if (StringUtils.isNotBlank(secret)) {
            long timestamp = DingtalkSignUtils.getTimestamp();
            String sign = DingtalkSignUtils.getSign(timestamp, secret);
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
     * 获取应用名
     *
     * @param instance 实例对象
     * @return 应用名
     */
    public static String getAppName(Instance instance) {
        return instance.getRegistration().getMetadata().getOrDefault(APP_NAME_KEY, instance.getRegistration().getName());
    }

    /**
     * 构建标题
     *
     * @param instance 实例对象
     * @return 标题
     */
    public static String buildTitle(Instance instance) {
        return MessageFormat.format(DEFAULT_TITLE_TEMPLATE, getAppName(instance));
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
                getAppName(instance) +
                "应用已下线" +
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
                getAppName(instance) +
                "应用已重新上线" +
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
                getAppName(instance) +
                "应用内存告警（" +
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
     * 构造cpu消息内容
     * @param instance 实例对象
     * @param cpuInfo cpu信息
     * @return cpu消息内容
     */
    public static String buildCpuContent(Instance instance, CpuInfo cpuInfo) {
        return "## " +
                getAppName(instance) +
                "CPU告警（" +
                cpuInfo.getWarningTitle() +
                "）" +
                LINE_FEED_QUOTE +
                "应用地址：" +
                instance.getRegistration().getServiceUrl() +
                LINE_FEED_QUOTE +
                "进程CPU使用率：" +
                cpuInfo.getProcessCpuUsage() +
                "%" +
                LINE_FEED_QUOTE +
                "系统CPU使用率：" +
                cpuInfo.getSystemCpuUsage() +
                "%" +
                LINE_FEED_QUOTE;
    }

    /**
     * 发送钉钉消息
     *
     * @param restTemplate       rest对象
     * @param dingtalkProperties 属性对象
     * @param instance           实例对象
     * @param content            内容
     */
    public static void sendDingTalkMes(RestTemplate restTemplate, DingtalkProperties dingtalkProperties, Instance instance, String content) {
        try {
            String dingtalkUrl = buildDingtalkUrl(dingtalkProperties, instance);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            DingtalkMdMessage mdMessage = DingtalkMdMessage.builder()
                    .title(buildTitle(instance))
                    .content(content)
                    .build();
            HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(mdMessage), headers);
            restTemplate.postForObject(dingtalkUrl, requestEntity, Object.class);
        } catch (Exception e) {
            log.error("钉钉消息发送异常:", e);
        }
    }

}
