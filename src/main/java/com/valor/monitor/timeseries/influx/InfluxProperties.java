package com.valor.monitor.timeseries.influx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * influxDB配置参数
 * @author liuzhaoming
 * @date 2022-05-26
 */
@ConfigurationProperties(prefix = "monitor.influx")
@Data
public class InfluxProperties {
    /**
     * 服务器URL，格式为"http://127.0.0.1:8086"
     */
    private String serverUrl;
}
