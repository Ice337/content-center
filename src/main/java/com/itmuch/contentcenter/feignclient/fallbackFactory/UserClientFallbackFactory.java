package com.itmuch.contentcenter.feignclient.fallbackFactory;

import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserCenterFeignClient> {

    @Override
    public UserCenterFeignClient create(Throwable throwable) {
        return new UserCenterFeignClient() {
            @Override
            public UserDTO findById(Integer id) {
                log.info("远程调用被限流了",throwable);
                UserDTO userDTO = new UserDTO();
                userDTO.setWxNickname("测试fallbackFactory");
                return userDTO;
            }
        };
    }
}
