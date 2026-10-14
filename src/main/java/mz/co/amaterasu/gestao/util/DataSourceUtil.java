package mz.co.amaterasu.gestao.util;

import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;

public class DataSourceUtil {


    public static DataSource createDataSource(String url, String username, String password) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver") // Use your specific DB driver
                .build();
    }
}

