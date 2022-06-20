package com.treta.bot.service;

import com.treta.bot.client.KeepUpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeepUpService {

    private final KeepUpClient keepUpClient;

    void keepBotAlive () {
        log.info("Iniciando keepAlive");
        keepUpClient.callBotUrl().block();
        log.info("Finalizando keepAlive");
    }
}
