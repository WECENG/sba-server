package com.tsintergy.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.Objects;
import com.tsieframework.core.base.math.BigDecimalFunctions;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * <p>
 * jvm信息请求工具类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 10:56
 */
public class JvmRequestUtil {

    public static final String MEASUREMENTS = "measurements";

    public static final String HEALTH_URI = "/health";

    public static final String VALUE = "value";

    public static final String MAX_HEAP_URI = "/metrics/jvm.memory.max";

    public static final String USED_HEAP_URI = "/metrics/jvm.memory.used";

    public static final String COMMITTED_HEAP_URI = "/metrics/jvm.memory.committed";

    public static final String HEAP_TAG = "tag=area:heap";

    public static final String NON_HEAP_TAG = "tag=area:nonheap";

    /**
     * 获取堆内存最大值,单位M
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param headers      请求头
     * @return 堆内存最大值
     */
    public static BigDecimal getMaxHeap(RestTemplate restTemplate, String serviceUrl, HttpHeaders headers) {
        return getJvmHeap(restTemplate, serviceUrl, MAX_HEAP_URI, HEAP_TAG, headers);
    }

    /**
     * 获取已使用堆内存,单位M
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param headers      请求头
     * @return 已使用堆内存
     */
    public static BigDecimal getUsedHeap(RestTemplate restTemplate, String serviceUrl, HttpHeaders headers) {
        return getJvmHeap(restTemplate, serviceUrl, USED_HEAP_URI, HEAP_TAG, headers);
    }

    /**
     * 获取已分配堆内存,单位M
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param headers      请求头
     * @return 已分配堆内存
     */
    public static BigDecimal getCommittedHeap(RestTemplate restTemplate, String serviceUrl, HttpHeaders headers) {
        return getJvmHeap(restTemplate, serviceUrl, COMMITTED_HEAP_URI, HEAP_TAG, headers);
    }

    /**
     * 获取非堆内存最大值,单位M
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param headers      请求头
     * @return 非堆内存最大值
     */
    public static BigDecimal getMaxNonHeap(RestTemplate restTemplate, String serviceUrl, HttpHeaders headers) {
        return getJvmHeap(restTemplate, serviceUrl, MAX_HEAP_URI, NON_HEAP_TAG, headers);
    }

    /**
     * 获取已使用非堆内存,单位M
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param headers      请求头
     * @return 已使用非堆内存
     */
    public static BigDecimal getUsedNonHeap(RestTemplate restTemplate, String serviceUrl, HttpHeaders headers) {
        return getJvmHeap(restTemplate, serviceUrl, USED_HEAP_URI, NON_HEAP_TAG, headers);
    }

    /**
     * 获取jvm信息
     *
     * @param restTemplate rest对象
     * @param serviceUrl   服务地址
     * @param uri          uri
     * @param tag          标签参数
     * @param headers      请求头
     * @return
     */
    public static BigDecimal getJvmHeap(RestTemplate restTemplate, String serviceUrl, String uri, String tag, HttpHeaders headers) {
        String reqUrl = serviceUrl + uri + "?" + tag;
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplate.exchange(reqUrl, HttpMethod.GET, httpEntity, String.class);
        if (entity.getStatusCode() == HttpStatus.OK && entity.hasBody()) {
            String body = entity.getBody();
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            if (Objects.nonNull(parse)) {
                JSONArray jsonArray = parse.getJSONArray(MEASUREMENTS);
                if (Objects.nonNull(jsonArray)) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    BigDecimal maxHeapBytes = jsonObject.getBigDecimal(VALUE);
                    BigDecimal unit = new BigDecimal(1024 * 1024);
                    return BigDecimalFunctions.divide(maxHeapBytes, unit);
                }
            }
        }
        return null;
    }

}
