package com.itmuch.contentcenter.service.content;

import com.alibaba.fastjson.JSON;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.dao.messaging.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.domain.entity.messaging.RocketmqTransactionLog;
import com.itmuch.contentcenter.domain.enums.AuditStatusEnum;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    private final UserCenterFeignClient userCenterFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;
    private final Source source;
    public ShareDTO findById(Integer id) {
        // 获取分享详情
        Share share = this.shareMapper.selectByPrimaryKey(id);
        // 发布人id
        Integer userId = share.getUserId();

//        // 1. 代码不可读
//        // 2. 复杂的url难以维护：https://user-center/s?ie={ie}&f={f}&rsv_bp=1&rsv_idx=1&tn=baidu&wd=a&rsv_pq=c86459bd002cfbaa&rsv_t=edb19hb%2BvO%2BTySu8dtmbl%2F9dCK%2FIgdyUX%2BxuFYuE0G08aHH5FkeP3n3BXxw&rqlang=cn&rsv_enter=1&rsv_sug3=1&rsv_sug2=0&inputT=611&rsv_sug4=611
//        // 3. 难以相应需求的变化，变化很没有幸福感
//        // 4. 编程体验不统一
//        UserDTO userDTO = this.userCenterFeignClient.findById(userId);
        /**
         * 通过Nacos调用user-center微服务
         */

//        String target = this.discoveryClient.getInstances("user-center").stream()
//                .map(serviceInstance -> serviceInstance.getUri().toString() + "/users/{id}")
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("当前没有实例"));


        /**
         * 手写一个简单的负载均衡器，这是客户端负载均衡，用的是随机
         */

//        List<String> collect = this.discoveryClient.getInstances("user-center").stream().
//                map(serviceInstance -> serviceInstance.getUri().toString() + "/users/{id}")
//                .collect(Collectors.toList());
//
//        int i = ThreadLocalRandom.current().nextInt(collect.size());
//        log.info("请求地址"+collect.get(i));
//        UserDTO forObject = restTemplate.getForObject(collect.get(i), UserDTO.class, userId);

//        UserDTO forObject = this.restTemplate.getForObject("http://user-center/users/{userId}", UserDTO.class, userId);

        UserDTO userDTO = this.userCenterFeignClient.findById(userId);
        ShareDTO shareDTO = new ShareDTO();
        // 消息的装配
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }

    public static void main(String[] args) {
//        RestTemplate restTemplate = new RestTemplate();
//        // 用HTTP GET方法去请求，并且返回一个对象
//        ResponseEntity<String> forEntity = restTemplate.getForEntity(
//            "http://localhost:8080/users/{id}",
//            String.class, 2
//        );
//
//        System.out.println(forEntity.getBody());
//        // 200 OK
//        // 500
//        // 502 bad gateway...
//        System.out.println(forEntity.getStatusCode());
    }

    public Share auditById(Integer id, ShareAuditDTO auditDTO) {
        String trans = UUID.randomUUID().toString();
        // 1. 查询share是否存在，不存在或者当前的audit_status != NOT_YET，那么抛异常
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null) {
            throw new IllegalArgumentException("参数非法！该分享不存在！");
        }
        if (!Objects.equals("NOT_YET", share.getAuditStatus())) {
            throw new IllegalArgumentException("参数非法！该分享已审核通过或审核不通过！");
        }
        //如果是Pass发送给mq
        if (AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())) {
           this.source.output().send(MessageBuilder.withPayload(UserAddBonusMsgDTO.builder()
                           .userId(share.getUserId()).bonus(50).build())
                           .setHeader(RocketMQHeaders.TRANSACTION_ID, trans)
                           .setHeader("share_id", id)
                           .setHeader("dto", JSON.toJSONString(auditDTO))
                           .build());

            //发送半消息
//            this.rocketMQTemplate.sendMessageInTransaction(
//                    "tx-add-bonus-group",
//                    "add-bonus", MessageBuilder.withPayload(UserAddBonusMsgDTO.builder()
//                            .userId(share.getUserId()).bonus(50).build())
//                            .setHeader(RocketMQHeaders.TRANSACTION_ID, trans)
//                            .setHeader("share_id", id)
//                            .setHeader("dto", JSON.toJSONString(auditDTO))
//                            .build(),
//                    auditDTO
//            );
        }else{
            this.auditByIdInDB(id, auditDTO);
        }

        return share;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInDB(Integer id, ShareAuditDTO shareAuditDTO) {
        Share share = Share.builder().id(id)
                .auditStatus(shareAuditDTO.getAuditStatusEnum().toString())
                .reason(shareAuditDTO.getReason())
                .build();
        int i = this.shareMapper.updateByPrimaryKeySelective(share);
        System.out.println(i);

    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdWithRocketMqLog(Integer id, ShareAuditDTO auditDTO, String transactionId) {
        this.auditByIdInDB(id, auditDTO);
        int i = this.rocketmqTransactionLogMapper.insertSelective(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .log("审核分享...")
                        .build()
        );
        System.out.println(i);
    }


}

