package com.itmuch.contentcenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign脱离Ribbon的使用方式
 */
@FeignClient(name = "baidu", url = "https://www.baidu.com")
public interface TestFeignWithoutRibbon {

    @GetMapping("")
    String testBaidu();

}
