package com.smartopd.websocket;

import com.smartopd.dto.response.QueueStatusResponse;
import com.smartopd.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class QueueWebSocketController {

    private final QueueService queueService;

    // Client subscribe kare aur live queue status le
    @MessageMapping("/queue.subscribe/{departmentId}")
    @SendTo("/topic/queue/{departmentId}")
    public QueueStatusResponse subscribeToQueue(
            @DestinationVariable Long departmentId) {
        return queueService.getQueueStatus(departmentId);
    }
}
