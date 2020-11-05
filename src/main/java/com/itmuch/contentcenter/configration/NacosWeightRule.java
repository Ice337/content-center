package com.itmuch.contentcenter.configration;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;

import javax.annotation.Resource;

@Slf4j
public class NacosWeightRule extends AbstractLoadBalancerRule {
    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object o) {
        try {
            BaseLoadBalancer baseLoadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            // 获取请求的微服务名称
            String name = baseLoadBalancer.getName();
            // 服务发现相关API
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            Instance instance = namingService.selectOneHealthyInstance(name);
            log.info("请求的实例是 : port = {} ,instance = {}", instance.getPort(), instance);
            return new NacosServer(instance);
        } catch (NacosException e) {
            return null;
        }
    }
}
