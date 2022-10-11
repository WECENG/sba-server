package com.tsintergy;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * <p>
 * sba server 启动类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/20 14:28
 */
@SpringBootApplication
@EnableAdminServer
@EnableWebSecurity
public class SbaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbaServerApplication.class, args);
    }

}
