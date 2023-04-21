package com.valor.monitor.timeseries.tdegine;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liuzhaoming
 * @date 2022-05-26
 */
@ConfigurationProperties(prefix = "monitor.tdengine")
@Data
public class TdengineProperties {
    /**
     * jdbc URL,
     * http 协议：jdbc:TAOS-RS://localhost:26041?user=root&password=taosdata
     * 原生协议：jdbc:TAOS://localhost:26030?user=root&password=taosdata
     */
    private String jdbcUrl = "jdbc:TAOS-RS://localhost:26041?user=root&password=taosdata";
}
