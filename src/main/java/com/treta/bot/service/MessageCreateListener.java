package com.treta.bot.service;

import com.treta.bot.listener.EventListener;
import com.treta.bot.listener.MessageListener;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageCreateListener extends MessageListener implements EventListener<MessageCreateEvent> {

    public MessageCreateListener (CommandMapRepository commandMapRepository) {

        super(commandMapRepository);
    }

    @Override
    public Class<MessageCreateEvent> getEventType () {

        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute (MessageCreateEvent event) {

        return processTextCommand(event.getMessage());
    }
}
