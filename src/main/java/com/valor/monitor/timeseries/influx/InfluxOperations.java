package com.valor.monitor.timeseries.influx;

import lombok.RequiredArgsConstructor;
import org.influxdb.InfluxDB;
import org.springframework.stereotype.Service;

/**
 * @author liuzhaoming
 * @date 2022-05-26
 */
//@Service
@RequiredArgsConstructor
public class InfluxOperations {
    private final InfluxDB influxDB;

}
