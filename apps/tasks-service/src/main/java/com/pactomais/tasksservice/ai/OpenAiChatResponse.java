package com.pactomais.tasksservice.ai;

import java.util.List;

record OpenAiChatResponse(List<Choice> choices) {

    record Choice(OpenAiChatRequest.Message message) {}
}
