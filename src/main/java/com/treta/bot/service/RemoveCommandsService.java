package com.treta.bot.service;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RemoveCommandsService {

    private final CommandMapRepository commandMapRepository;

    public Mono<CommandDTO> processRemoveCommand (Message message) {

        String commandName = Arrays.asList(message.getContent().split(" ")).get(1);

        return commandMapRepository.findByCommandName(commandName)
                .flatMap(map -> buildCommandDTO(map, message))
                .flatMap(this::removeCommand);
    }

    private Mono<CommandDTO> buildCommandDTO (CommandMap commandMap, Message message) {

        commandMap.setCommandReply("Comando removido com sucesso.");

        return Mono.just(CommandDTO.builder()
                .commandType(CommandType.ADMIN)
                .adminCommand(AdminCommands.REMOVE)
                .commandMap(commandMap)
                .message(message)
                .build());
    }

    private Mono<CommandDTO> removeCommand (CommandDTO commandDTO) {

        return commandMapRepository.delete(commandDTO.getCommandMap())
                .thenReturn(commandDTO);
    }
}
