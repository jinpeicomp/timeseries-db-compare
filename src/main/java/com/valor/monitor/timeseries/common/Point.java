package com.valor.monitor.timeseries.common;

import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 采样点
 *
 * @author liuzhaoming
 * @date 2022-05-30
 */
@Data
public class Point {
    private String name;

    private Map<String, String> tags;

    private Map<String, Object> fields;

    private Long timestamp;

    public void addTag(String name, String value) {
        if (null == tags) {
            tags = new TreeMap<>();
        }

        tags.put(name, value);
    }

    public void addField(String name, Object value) {
        if (null == fields) {
            fields = new TreeMap<>();
        }

        fields.put(name, value);
    }

    public String joinTagName(String joinChar) {
        return String.join(joinChar, tags.keySet());
    }

    public String joinTagValue(String joinChar) {
        return String.join(joinChar, tags.values());
    }

    public String joinFieldNameWithTime(String joinChar) {
        return "time," + String.join(joinChar, fields.keySet());
    }

    public String joinFieldValueWithTime(String joinChar) {
        String strValues = fields.values()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(joinChar));
        return timestamp + "," + strValues;
    }

    /**
     * 从InfluxDB Line 协议中解析数据
     * 格式为："cpu,cpu=cpu-total,host=METRIC-OVH-CA-ES02-01 usage_guest=0 1653408530000000000"
     *
     * @param line     InfluxDB Line 协议字符串
     * @param timeUnit 时间戳单位
     * @return 指标数据
     */
    public static Point fromInfluxLine(String line, TimeUnit timeUnit) {
        String[] segments = line.split(" ");
        if (segments.length != 3) {
            return null;
        }

        Point point = new Point();
        String[] nameAndTags = segments[0].split(",");
        point.setName(nameAndTags[0]);
        for (String tagNameAndValue : nameAndTags) {
            String[] array = tagNameAndValue.split("=");
            if (array.length != 2) {
                continue;
            }
            point.addTag(array[0], array[1]);
        }

        for (String fieldNameAndValue : segments[1].split(",")) {
            String[] array = fieldNameAndValue.split("=");
            if (NumberUtils.isCreatable(array[0])) {
                point.addField(array[0], NumberUtils.toDouble(array[1]));
            } else {
                point.addField(array[0], array[1]);
            }
        }

        if (timeUnit == TimeUnit.NANOSECONDS) {
            point.setTimestamp(Long.parseLong(segments[2].substring(0, segments[2].length() - 6)));
        } else if (timeUnit == TimeUnit.MICROSECONDS) {
            point.setTimestamp(Long.parseLong(segments[2].substring(0, segments[2].length() - 3)));
        } else if (timeUnit == TimeUnit.MILLISECONDS) {
            point.setTimestamp(Long.parseLong(segments[2]));
        } else if (timeUnit == TimeUnit.SECONDS) {
            point.setTimestamp(Long.parseLong(segments[2]) * 1000);
        } else if (timeUnit == TimeUnit.MINUTES) {
            point.setTimestamp(Long.parseLong(segments[2]) * 1000000);
        } else {
            return null;
        }

        return point;
    }
}
