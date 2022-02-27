package com.treta.bot.service;

import com.treta.bot.client.KeepUpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class KeepUpService {

    private final KeepUpClient keepUpClient;

    @Scheduled(fixedRate = 1200 * 1000)
    void keepBotAlive () {
        log.info("Iniciando keepAlive");
        keepUpClient.callBotUrl().block();
        log.info("Finalizando keepAlive");
    }
}
