package com.treta.bot.service;

import com.treta.bot.listener.EventListener;
import com.treta.bot.listener.MessageListener;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class MessageCreateListener extends MessageListener implements EventListener<MessageCreateEvent> {

    private final String ADD_NEW_COMMAND = "addCommand";

    @Value("${bot.prefix}")
    private String prefix;

    public MessageCreateListener (CommandMapRepository commandMapRepository) {

        super(commandMapRepository);
    }

    @Override
    public Class<MessageCreateEvent> getEventType () {

        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute (MessageCreateEvent event) {

        return Mono.just(event.getMessage())
                .filter(msg -> msg.getContent().startsWith(prefix))
                .filter(msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(msg -> {
                    String commandName = Arrays.asList(msg.getContent().split(" ")).get(0).substring(1);
                    if (!ADD_NEW_COMMAND.equals(commandName)) {
                        return processTextCommand(msg, commandName);
                    }
                    else {
                        return addNewTextCommand(msg);
                    }
                });
    }
}
