package com.itmuch.contentcenter.configration;

import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * 日志级别配置类
 * 特别注意：这个类不需要加@Configuration注解
 */
public class UserCenterFeignConfiguration {
    @Bean
    public Logger.Level level() {
        return Logger.Level.FULL;
    }
}
