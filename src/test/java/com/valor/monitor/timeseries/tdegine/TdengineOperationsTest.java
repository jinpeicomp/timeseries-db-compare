package com.valor.monitor.timeseries.tdegine;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Tdengine 操作接口单元测试
 *
 * @author liuzhaoming
 * @date 2022-06-01
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TdengineOperationsTest {

    @Autowired
    private TdengineOperations tdengineOperations;

    @Test
    public void initDataFromInflux() {
        String fileName = "/Users/liuzhaoming/software/time-series/data/estelegraf.row";
        tdengineOperations.initDataFromInflux(fileName, "estelegraf", "cpu");
    }

    @Test
    public void query(){
        String sql = "select avg(usage_system) as usage_system from estelegraf.cpu where (time >= '2022-05-24 16:13:00.000' AND time < '2022-05-24 16:14:00.000') and host = 'METRIC-OVH-US-PROXY' interval(1m) group by tbname, host, cpu slimit 1\n" +
                "union all select avg(usage_system) as usage_system from estelegraf.cpu where (time >= '2022-05-24 16:23:00.000' AND time < '2022-05-24 16:24:00.000') and host = 'METRIC-OVH-US-PROXY' interval(1m) group by tbname, host, cpu  slimit 1; ";
        List list = tdengineOperations.select(sql);
        log.info("===========> length {}", list.size());
    }
}