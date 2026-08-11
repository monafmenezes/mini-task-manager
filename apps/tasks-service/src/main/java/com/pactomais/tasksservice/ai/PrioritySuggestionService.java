package com.pactomais.tasksservice.ai;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Service
public class PrioritySuggestionService {

    private static final Logger log = LoggerFactory.getLogger(PrioritySuggestionService.class);

    private static final String SYSTEM_PROMPT =
            """
            Você é um assistente que ajuda a organizar tarefas de um sistema de gestão de \
            projetos para times pequenos. Dado o título e (opcionalmente) a descrição de uma \
            tarefa, responda APENAS com um JSON no formato exato:
            {"prioridade": "BAIXA" | "MEDIA" | "ALTA", "justificativa": "uma frase curta em \
            português explicando o motivo da prioridade", "descricaoSugerida": "uma descrição \
            objetiva de 1 a 3 frases em português para a tarefa, baseada no título e na \
            descrição fornecida (se houver)"}
            Não inclua nenhum texto fora desse JSON.""";

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public PrioritySuggestionService(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient =
                RestClient.builder()
                        .baseUrl(properties.baseUrl())
                        .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                        .build();
    }

    public boolean isDisponivel() {
        return properties.apiKey() != null && !properties.apiKey().isBlank();
    }

    public PrioritySuggestionResponse suggest(PrioritySuggestionRequest request) {
        String userMessage =
                "Título: "
                        + request.titulo()
                        + "\nDescrição: "
                        + (request.descricao() == null ? "(sem descrição)" : request.descricao());

        OpenAiChatRequest chatRequest =
                new OpenAiChatRequest(
                        properties.model(),
                        List.of(
                                new OpenAiChatRequest.Message("system", SYSTEM_PROMPT),
                                new OpenAiChatRequest.Message("user", userMessage)),
                        new OpenAiChatRequest.ResponseFormat("json_object"),
                        0.2);

        try {
            OpenAiChatResponse response =
                    restClient
                            .post()
                            .uri("/chat/completions")
                            .body(chatRequest)
                            .retrieve()
                            .body(OpenAiChatResponse.class);

            String content = response.choices().get(0).message().content();
            return objectMapper.readValue(content, PrioritySuggestionResponse.class);
        } catch (Exception e) {
            log.error("Falha ao chamar a API da OpenAI", e);
            throw new AiSuggestionException(
                    "Não foi possível obter a sugestão de prioridade da IA", e);
        }
    }
}
