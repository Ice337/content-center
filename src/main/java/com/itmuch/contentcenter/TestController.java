package com.itmuch.contentcenter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.feignclient.TestFeignWithoutRibbon;
import com.itmuch.contentcenter.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class TestController {
    @Autowired(required = false)
    private ShareMapper shareMapper;
    @Resource
    private DiscoveryClient discoveryClient;
    @Resource
    private TestFeignWithoutRibbon testFeignWithoutRibbon;
    @Autowired
    private Source source ;


    @GetMapping("/test")
    public List<Share> testInsert() {
        // 1. 做插入
        Share share = new Share();
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        share.setTitle("xxx");
        share.setCover("xxx");
        share.setAuthor("大目");
        share.setBuyCount(1);

        this.shareMapper.insertSelective(share);

        // 2. 做查询: 查询当前数据库所有的share  select * from share ;
        List<Share> shares = this.shareMapper.selectAll();

        return shares;
    }


    @GetMapping("getClient")
    public List<ServiceInstance> getClient() {
        List<ServiceInstance> instances = this.discoveryClient.getInstances("user-center");
        return instances;
    }


    @GetMapping("/testBaidu")
    public String testBaidu() {
        return testFeignWithoutRibbon.testBaidu();
    }

    @GetMapping("/testHot")
    @SentinelResource("hot")
    public String testHot(@RequestParam(required = false) String a, @RequestParam(required = false) String b) {
        String str = "a=" + a;
        String str2 = "b=" + b;
        return str.concat(str2);
    }

    @GetMapping("/test-sentinel-api")
    public String testSentinel(@RequestParam(required = false) String a) {
        Entry entry = null;
        try {
            entry = SphU.entry("test-sentinel-api");
            if (StringUtils.isBlank(a)) {
                throw new IllegalArgumentException("a不能为空");
            }
            return a;
        } catch (BlockException e) {
            log.warn("限流或者降级了");
            return "限流了,或者降级了";
        } catch (IllegalArgumentException e2) {
            Tracer.trace(e2);
            return "参数非法";

        } finally {
            if (entry != null) {
                entry.exit();

            }
        }

    }


    @GetMapping("/test-sentinel-resource")
    @SentinelResource(value = "test-sentinel-resource", blockHandler = "block", fallback = "fallback")
    public String testResource(@RequestParam(required = false) String a) {

        if (StringUtils.isBlank(a)) {
            throw new IllegalArgumentException("params can not be null");
        }
        return a;

    }

    /**
     * 限流
     *
     * @param a
     * @param e
     * @return
     */
    public String block(String a, BlockException e) {
        log.warn("现在被限流或者被降级");
        return "现在被限流或者被降级 block";
    }

    /**
     * 降级
     *
     * @param a
     * @return
     */
    public String fallback(String a) {
        log.warn("现在被限流或者被降级");
        return "现在被限流或者被降级 fallback";
    }

    @GetMapping("test-stream")
    public String testStream() {
        this.source.output().send(MessageBuilder.withPayload("消息体").build());
        return "200";
    }


}
