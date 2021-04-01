package com.treta.bot.listener;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class MessageListener {

    private final String HELP_MSG = "__**Lista de memes:**__\n\n`$acabou`";

    public Mono<Void> processCommand(Message eventMessage) {

        return Mono.just(eventMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(message -> message.getContent().equalsIgnoreCase("$help"))
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createMessage(HELP_MSG))
                .then();
    }
}
