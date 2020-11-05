package com.itmuch.contentcenter.rocketmq;

import com.alibaba.fastjson.JSON;
import com.itmuch.contentcenter.dao.messaging.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.entity.messaging.RocketmqTransactionLog;
import com.itmuch.contentcenter.service.content.ShareService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import javax.annotation.Resource;

@RocketMQTransactionListener(txProducerGroup = "tx-add-bonus-group")
public class AddBonusTransactionalListener implements RocketMQLocalTransactionListener {
    @Resource
    private ShareService shareService;
    @Resource
    private RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        MessageHeaders headers = message.getHeaders();
        String transId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        Integer shareId = Integer.valueOf((String) headers.get("share_id"));
        String dtoString = (String) headers.get("dto");
        ShareAuditDTO auditDTO = JSON.parseObject(dtoString, ShareAuditDTO.class);
        try {
            this.shareService.auditByIdWithRocketMqLog(shareId, auditDTO, transId);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);

        // select * from xxx where transaction_id = xxx
        RocketmqTransactionLog transactionLog = this.rocketmqTransactionLogMapper.selectOne(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .build()
        );
        if (transactionLog != null) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
