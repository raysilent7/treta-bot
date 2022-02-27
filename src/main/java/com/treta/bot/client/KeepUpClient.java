package com.treta.bot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class KeepUpClient {

    private final WebClient webClient;

    public KeepUpClient () {
        this.webClient = WebClient.create("https://treta-boterson.herokuapp.com/");
    }

    public Mono<ResponseEntity<Void>> callBotUrl () {
        return webClient.get()
                .retrieve()
                .toBodilessEntity();
    }
}
