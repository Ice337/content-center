package com.itmuch.contentcenter.configration;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;
import ribbionconfigruation.RibbonConfiguration;

/**
 * 全局负载均衡策略配置
 */
@Configuration
@RibbonClient(name = "user-center",configuration = RibbonConfiguration.class)
public class UserCenterRibbonConfiguration {

}
