package com.pactomais.tasksservice.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record OpenAiChatRequest(
        String model,
        List<Message> messages,
        @JsonProperty("response_format") ResponseFormat responseFormat,
        Double temperature) {

    record Message(String role, String content) {}

    record ResponseFormat(String type) {}
}
