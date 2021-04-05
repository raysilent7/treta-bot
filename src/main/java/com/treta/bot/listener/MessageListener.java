package com.treta.bot.listener;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@RequiredArgsConstructor
public abstract class MessageListener {

    protected final CommandMapRepository commandMapRepository;

    public Mono<Void> processTextCommand (Message eventMessage) {

        return Mono.just(eventMessage)
                .flatMap(message -> resolveCommand(message, CommandType.TEXT))
                .flatMap(this::replyCommand)
                .then();
    }

    public Mono<Void> addNewTextCommand (Message eventMessage) {

        return commandMapRepository.findCommandMapByCommandType(CommandType.TEXT)
                .switchIfEmpty(buildCommandMap(eventMessage))
                .flatMap(commandMap -> resolveArgs(commandMap, eventMessage))
                .flatMap(this::saveNewCommand)
                .flatMap(this::replyCommand)
                .then();
    }

    private Mono<CommandMap> buildCommandMap (Message message) {

        return Mono.just(CommandMap.builder()
                .LastModifiedDate(LocalDateTime.now())
                .commandType(CommandType.TEXT)
                .commands(new HashMap<>())
                .build());
    }

    private Mono<CommandDTO> saveNewCommand (CommandDTO commandDTO) {

        commandDTO.setCommandType(CommandType.ADD);
        commandDTO.getCommandMap().getCommands().put(commandDTO.getArgs().get(1), commandDTO.getArgs().get(2));
        return commandMapRepository.save(commandDTO.getCommandMap())
                .then(Mono.just(commandDTO));
    }

    private Mono<CommandDTO> resolveArgs (CommandMap commandMap, Message message) {

        CommandDTO commandDTO = CommandDTO.builder()
                .commandMap(commandMap)
                .message(message)
                .args(new ArrayList<>())
                .build();

        commandDTO.setArgs(Arrays.asList(message.getContent().split(" ")));
        return Mono.just(commandDTO);
    }

    private Mono<CommandDTO> resolveCommand (Message message, CommandType type) {

        return commandMapRepository.findCommandMapByCommandType(type)
                .flatMap(commandMap -> resolveArgs(commandMap, message))
                .filter(dto -> dto.getCommandMap().getCommands().containsKey(dto.getArgs().get(0).substring(1)))
                .flatMap(dto -> {
                    dto.setCommandType(CommandType.TEXT);
                    return Mono.just(dto);
                })
                .flatMap(this::resolveAuthor);
    }

    private Mono<Message> replyCommand (CommandDTO commandDTO) {

        return Mono.just(commandDTO)
                .flatMap(dto -> dto.getMessage().getChannel())
                .flatMap(channel -> reply(channel, commandDTO));
    }

    private Mono<Message> reply (MessageChannel channel, CommandDTO commandDTO) {

        if (CommandType.ADD.equals(commandDTO.getCommandType())) {
            return channel.createMessage(commandDTO.getCommandMap().getCommands().get(commandDTO.getArgs().get(1)));
        }
        else {
            return channel.createMessage(commandDTO.getCommandMap().getCommands().get(commandDTO.getArgs().get(0).substring(1)));
        }
    }

    private Mono<CommandDTO> resolveAuthor (CommandDTO commandDTO) {

        if (commandDTO.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false)) {
            return Mono.just(commandDTO);
        }
        else {
            return Mono.empty();
        }
    }
}
