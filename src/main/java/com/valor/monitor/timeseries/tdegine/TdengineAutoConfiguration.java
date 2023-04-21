package com.valor.monitor.timeseries.tdegine;

import com.taosdata.jdbc.SchemalessWriter;
import com.taosdata.jdbc.TSDBDriver;
import com.taosdata.jdbc.enums.SchemalessProtocolType;
import com.taosdata.jdbc.enums.SchemalessTimestampType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author liuzhaoming
 * @date 2022-05-26
 */
@EnableConfigurationProperties(TdengineProperties.class)
@Configuration
public class TdengineAutoConfiguration {
    @Bean
    public Connection tdengineConnection(TdengineProperties properties) throws SQLException {
        Properties connProps = new Properties();
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8");
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_LOCALE, "en_US.UTF-8");
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8");
        return DriverManager.getConnection(properties.getJdbcUrl(), connProps);
    }

    public static void main(String[] args) {
        TdengineAutoConfiguration configuration = new TdengineAutoConfiguration();
        try (Connection connection = configuration.tdengineConnection(new TdengineProperties());
             Statement stmt = connection.createStatement();
        ) {
            stmt.execute("USE estelegraf");
            String path = "/Users/liuzhaoming/software/time-series/data/estelegraf.row";
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while (null != (line = br.readLine())) {
                if (line.startsWith("#")) {
                    continue;
                }
                try {
                    SchemalessWriter writer = new SchemalessWriter(connection);
                    writer.write(line, SchemalessProtocolType.LINE, SchemalessTimestampType.NANO_SECONDS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
