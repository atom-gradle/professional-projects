package com.qian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qian.dto.AnalysisResultMessage;
import com.qian.dto.AnalysisTaskRequest;
import com.qian.config.RabbitMQConfig;
import com.qian.pojo.AnalysisReport;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AnalysisResultConsumerService {

    private final AnalysisService analysisServiceImpl;

    private final ObjectMapper objectMapper;

    public AnalysisResultConsumerService(AnalysisService analysisServiceImpl, ObjectMapper objectMapper) {
        this.analysisServiceImpl = analysisServiceImpl;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听结果队列，接收Python算法服务返回的结果
     */
    @RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void consumeResult(AnalysisResultMessage result, Message message, Channel channel) {
        try {
            log.info("收到计算结果: taskId={}, 结果={}, 耗时={}ms",
                    result.getTaskId(),
                    result.getResultUrl(),
                    result.getElapsedTime());

            analysisServiceImpl.completeTask(result.getTaskId(), result);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("处理结果失败: {}", e.getMessage(), e);
            try {
                // 拒收消息，不重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                log.error("确认消息失败", ex);
            }
        }
    }

    /**
     * 处理死信队列的消息
     */
    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(Message message, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String body = new String(message.getBody());
            log.error("收到死信消息: {}", body);

            try {
                AnalysisTaskRequest task = objectMapper.readValue(body, AnalysisTaskRequest.class);
                String captureId = task.getCaptureId();

                // 更新数据库状态为失败
                AnalysisReport report = analysisServiceImpl.getOne(
                        new LambdaQueryWrapper<AnalysisReport>()
                                .eq(AnalysisReport::getCaptureId, captureId)
                );

                if (report != null) {
                    report.setStatus(3);// 失败状态
                    report.setErrorMessage("任务处理失败，已进入死信队列");
                    report.setUpdateTime(LocalDateTime.now());
                    analysisServiceImpl.updateById(report);
                }

                analysisServiceImpl.completeTaskExceptionally(captureId, "任务处理失败");

            } catch (Exception e) {
                log.error("解析死信消息失败", e);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理死信消息失败", e);
            try {
                // 如果处理死信也失败，记录日志后确认，避免无限循环
                channel.basicAck(deliveryTag, false);
            } catch (IOException ex) {
                log.error("确认死信消息失败", ex);
            }
        }
    }
}
