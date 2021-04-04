package com.treta.bot.listener;

import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class MessageListener {

    protected final CommandMapRepository commandMapRepository;

    public Mono<Void> processTextCommand (Message eventMessage) {

        return Mono.just(eventMessage)
                .flatMap(message-> resolveCommand(message, CommandType.TEXT))
                .flatMap(this::replyCommand)
                .then();
    }

    private Mono<CommandMap> resolveCommand (Message message, CommandType type) {

        return commandMapRepository.findCommandMapByCommandType(type)
                .filter(commandMap -> commandMap.getCommands().containsKey(message.getContent().substring(1)))
                .flatMap(commandMap -> resolveAuthor(message, commandMap));
    }

    private Mono<Message> replyCommand (CommandMap commandMap) {

        return Mono.just(commandMap)
                .flatMap(map -> map.getMessage().getChannel())
                .flatMap(channel -> channel.createMessage(commandMap.getCommands().get(commandMap.getMessage().getContent().substring(1))));
    }

    private Mono<CommandMap> resolveAuthor (Message message, CommandMap commandMap) {

        if (message.getAuthor().map(user -> !user.isBot()).orElse(false)) {
            commandMap.setMessage(message);
            return Mono.just(commandMap);
        }
        else {
            return Mono.empty();
        }
    }
}
