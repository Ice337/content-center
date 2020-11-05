package com.itmuch.contentcenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {
    public String common() {
        log.info("common....");
        return "common";
    }

}
