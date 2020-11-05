package com.itmuch.contentcenter.feignclient;

import com.itmuch.contentcenter.configration.UserCenterFeignConfiguration;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.fallback.UserCenterFeginClientFallback;
import com.itmuch.contentcenter.feignclient.fallbackFactory.UserClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 使用Feign作为Http客户端
 */
@FeignClient(name = "user-center",configuration = UserCenterFeignConfiguration.class,fallbackFactory = UserClientFallbackFactory.class)
public interface UserCenterFeignClient {
    /**
     * http://user-center/users/{id}
     *
     * @param id
     * @return
     */
    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable Integer id);
    
}
