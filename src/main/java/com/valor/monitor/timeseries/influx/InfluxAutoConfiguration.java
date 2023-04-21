package com.valor.monitor.timeseries.influx;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * influxDB自动配置类
 * @author liuzhaoming
 * @date 2022-05-26
 */
@EnableConfigurationProperties(InfluxProperties.class)
@Configuration
public class InfluxAutoConfiguration {
//    @Bean
    public InfluxDB influxClient(InfluxProperties properties){
        return InfluxDBFactory.connect(properties.getServerUrl());
    }
}
