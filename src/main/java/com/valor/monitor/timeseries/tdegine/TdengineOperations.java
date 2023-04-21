package com.valor.monitor.timeseries.tdegine;

import com.valor.monitor.timeseries.common.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TDengine 数据库操作封装类
 *
 * @author liuzhaoming
 * @date 2022-05-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TdengineOperations {
    private final Connection connection;

    public List select(String sql) {
        try (Statement statement = connection.createStatement()) {
            List list = new ArrayList();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getObject(1));
            }
            return list;
        } catch (Exception e) {
            log.error("Select has error {}", sql, e);
            return Collections.emptyList();
        }
    }

    public void initDataFromInflux(String fileName, String database, String tableName) {
        try (FileReader fr = new FileReader(fileName);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            String tableNameStarter = tableName + ",";
            List<Point> points = new ArrayList<>(1000);
            String actualTableName = database + "." + tableName;
            int threadNum = 5;
            ThreadPoolExecutor executor = new ThreadPoolExecutor(threadNum, threadNum, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(30));
            int i = 1;
            while (null != (line = br.readLine())) {
                if (!line.startsWith(tableNameStarter)) {
                    continue;
                }

                Point point = Point.fromInfluxLine(line, TimeUnit.NANOSECONDS);
                if (null != point) {
                    points.add(point);
                }

                if (points.size() >= 5000) {
                    log.info("========> current position {}", 5000 * i++);
                    List<Point> pointList = points;
                    while (executor.getQueue().size() >= 10) {
                        Thread.sleep(10);
                    }
                    executor.submit(() -> insert(database, actualTableName, pointList));
                    points = new ArrayList<>(1000);
                }
            }

            if (!points.isEmpty()) {
                List<Point> pointList = points;
                executor.submit(() -> insert(database, actualTableName, pointList));
            }
        } catch (Exception e) {
            log.error("Init data from influx error {}", fileName, e);
        }
    }

    private void insert(String database, String actualTableName, List<Point> points) {
        String detailSql = points
                .stream()
                .map(currentPoint -> {
                    String subTableName = database + "." + currentPoint.getName() + "_" +
                            currentPoint.joinTagValue("_").replaceAll("-", "_");
                    String strTagNames = currentPoint.joinTagName(",");
                    String strTagValues = "\"" + currentPoint.joinTagValue("\",\"") + "\"";
                    String strFiledName = currentPoint.joinFieldNameWithTime(",");
                    String strFieldValue = currentPoint.joinFieldValueWithTime(",");
                    //d21003 USING meters (groupId) TAGS (2) (ts, current, phase) VALUES ()
                    return String.format("%s USING %s (%s) TAGS (%s) (%s) VALUES (%s)",
                            subTableName, actualTableName, strTagNames, strTagValues, strFiledName, strFieldValue);
                }).collect(Collectors.joining("\n"));
        String sql = "INSERT INTO " + detailSql;

        try (Statement statement = connection.createStatement()) {
            int rowNumber = statement.executeUpdate(sql);
            if (rowNumber != points.size()) {
                log.error("=======> Some point maybe insert error row number {} point number {}",
                        rowNumber, points.size());
            }
        } catch (Exception e) {
            log.error("=======> Execute sql error", e);
        }
    }
}
