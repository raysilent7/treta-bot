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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddCommandsService {

    public final CommandMapRepository commandMapRepository;

    public Mono<CommandDTO> addNewTextCommand (Message message) {

        return buildCommandMap(CommandType.TEXT, message)
                .flatMap(commandMap -> commandMapRepository.findByCommandName(commandMap.getCommandName()))
                .switchIfEmpty(buildCommandMap(CommandType.TEXT, message))
                .flatMap(commandMap -> resolveArgs(commandMap, message))
                .flatMap(this::saveNewCommand);
    }

    private Mono<CommandMap> buildCommandMap (CommandType type, Message message) {

        return Mono.just(CommandMap.builder()
                .commandName(Arrays.asList(message.getContent().split(" ")).get(1))
                .LastModifiedDate(LocalDateTime.now())
                .commandType(type)
                .build());
    }

    private Mono<CommandDTO> saveNewCommand (CommandDTO commandDTO) {

        commandDTO.setCommandType(CommandType.ADMIN);
        commandDTO.setAdminCommand(AdminCommands.ADD);
        return commandMapRepository.save(commandDTO.getCommandMap())
                .then(Mono.just(commandDTO));
    }

    private Mono<CommandDTO> resolveArgs (CommandMap commandMap, Message message) {

        List<String> args = new LinkedList<>(Arrays.asList(message.getContent().split(" ")));
        commandMap.setCommandName(args.get(1));
        commandMap.setCommandDescription(args.get(2));
        commandMap.resolveReply(args);

        return Mono.just(CommandDTO.builder()
                .commandMap(commandMap)
                .message(message)
                .build());
    }
}
