package com.pactomais.tasksservice.ai;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class PrioritySuggestionController {

    private final PrioritySuggestionService prioritySuggestionService;

    @GetMapping("/sugerir-prioridade/disponivel")
    public AiAvailabilityResponse disponibilidade() {
        return new AiAvailabilityResponse(prioritySuggestionService.isDisponivel());
    }

    @PostMapping("/sugerir-prioridade")
    public PrioritySuggestionResponse suggest(@Valid @RequestBody PrioritySuggestionRequest request) {
        return prioritySuggestionService.suggest(request);
    }
}
