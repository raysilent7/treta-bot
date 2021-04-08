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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public abstract class MessageListener {

    protected final CommandMapRepository commandMapRepository;

    public Mono<Void> processTextCommand (Message eventMessage, String commandName) {

        return resolveCommand(eventMessage, commandName)
                .flatMap(this::replyCommand)
                .then();
    }

    public Mono<Void> addNewTextCommand (Message eventMessage) {

        return buildCommandMap(CommandType.TEXT, eventMessage)
                .flatMap(commandMap -> commandMapRepository.findByCommandName(commandMap.getCommandName()))
                .switchIfEmpty(buildCommandMap(CommandType.TEXT, eventMessage))
                .flatMap(commandMap -> resolveArgs(commandMap, eventMessage))
                .flatMap(this::saveNewCommand)
                .flatMap(this::replyCommand)
                .then();
    }

    private Mono<CommandMap> buildCommandMap (CommandType type, Message message) {

        return Mono.just(CommandMap.builder()
                .commandName(Arrays.asList(message.getContent().split(" ")).get(1))
                .LastModifiedDate(LocalDateTime.now())
                .commandType(type)
                .build());
    }

    private Mono<CommandDTO> saveNewCommand (CommandDTO commandDTO) {

        commandDTO.setCommandType(CommandType.ADD);
        return commandMapRepository.save(commandDTO.getCommandMap())
                .then(Mono.just(commandDTO));
    }

    private Mono<CommandDTO> resolveArgs (CommandMap commandMap, Message message) {

        List<String> args = new LinkedList<>(Arrays.asList(message.getContent().split(" ")));
        commandMap.setCommandName(args.get(1));
        commandMap.resolveReply(args);

        return Mono.just(CommandDTO.builder()
                .commandMap(commandMap)
                .message(message)
                .build());
    }

    private Mono<CommandDTO> resolveCommand (Message message, String commandName) {

        return commandMapRepository.findByCommandName(commandName)
                .flatMap(commandMap -> Mono.just(CommandDTO.builder()
                        .commandType(CommandType.TEXT)
                        .commandMap(commandMap)
                        .message(message)
                        .build()));
    }

    private Mono<Message> replyCommand (CommandDTO commandDTO) {

        return Mono.just(commandDTO)
                .flatMap(dto -> dto.getMessage().getChannel())
                .flatMap(channel -> reply(channel, commandDTO));
    }

    private Mono<Message> reply (MessageChannel channel, CommandDTO commandDTO) {

        if (CommandType.ADD.equals(commandDTO.getCommandType())) {
            return channel.createMessage(commandDTO.returnSuccessReply());
        }
        else {
            return channel.createMessage(commandDTO.getCommandMap().getCommandReply());
        }
    }
}
