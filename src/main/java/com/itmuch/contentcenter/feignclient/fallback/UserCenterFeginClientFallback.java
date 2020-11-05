package com.itmuch.contentcenter.feignclient.fallback;

import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import org.springframework.stereotype.Component;

@Component
public class UserCenterFeginClientFallback implements UserCenterFeignClient {
    @Override
    public UserDTO findById(Integer id) {
        UserDTO userDTO = new UserDTO();
        userDTO.setWxNickname("一个默认用户");
        return userDTO;
    }
}
