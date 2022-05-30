package com.treta.bot.service;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TextCommandsService {

    public Mono<CommandDTO> processTextCommand (Message message, CommandMap commandMap) {
        return resolveCommand(message, commandMap);
    }

    private Mono<CommandDTO> resolveCommand (Message message, CommandMap commandMap) {
        return Mono.just(CommandDTO.builder()
                .commandType(CommandType.TEXT)
                .adminCommand(AdminCommands.NORMAL)
                .commandMap(commandMap)
                .message(message)
                .build());
    }
}
