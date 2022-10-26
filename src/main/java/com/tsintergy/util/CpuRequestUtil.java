package com.tsintergy.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * cpu信息请求工具类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/10/25 16:04
 */
@Slf4j
public class CpuRequestUtil {

    public static final String HEALTH_URI = "/health";

    public static final String MEASUREMENTS = "measurements";

    public static final String VALUE = "value";

    public static final String PROCESS_CPU_USAGE_URI = "/metrics/process.cpu.usage";

    public static final String SYSTEM_CPU_USAGE_URI = "/metrics/system.cpu.usage";

    /**
     * 获取进程cpu使用率，单位%
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param request      原始请求
     * @return 进程cpu使用率
     */
    public static BigDecimal getProcessCpuUsage(RestTemplate restTemplate, String serviceUrl, ClientRequest request) {
        return getCpuUsage(restTemplate, serviceUrl, PROCESS_CPU_USAGE_URI, request);
    }

    /**
     * 获取系统cpu使用率，单位%
     *
     * @param restTemplate rest对象
     * @param serviceUrl   实例服务url
     * @param request      原始请求
     * @return 系统cpu使用率
     */
    public static BigDecimal getSystemCpuUsage(RestTemplate restTemplate, String serviceUrl, ClientRequest request) {
        return getCpuUsage(restTemplate, serviceUrl, SYSTEM_CPU_USAGE_URI, request);
    }


    private static BigDecimal getCpuUsage(RestTemplate restTemplate, String serviceUrl, String uri, ClientRequest request) {
        try {
            String reqUrl = serviceUrl + uri;
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(request.headers());
            List<String> cookies = new ArrayList<>();
            request.cookies().forEach((key, name) -> cookies.add(key + "=" + String.join(";", name)));
            headers.putIfAbsent(HttpHeaders.COOKIE, cookies);
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
                        BigDecimal percent = new BigDecimal(100);
                        return Objects.isNull(maxHeapBytes) ? null : maxHeapBytes.multiply(percent).divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
                    }
                }
            }
        } catch (Exception e) {
            log.error("cpu信息监控异常:", e);
        }
        return null;
    }

}
