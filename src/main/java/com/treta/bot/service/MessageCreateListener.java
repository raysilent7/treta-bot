package com.treta.bot.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.treta.bot.config.LavaPlayerAudioProvider;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.listener.EventListener;
import com.treta.bot.listener.MessageListener;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.voice.AudioProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class MessageCreateListener extends MessageListener implements EventListener<MessageCreateEvent> {

    private final AudioPlayerManager playerManager;

    public MessageCreateListener (CommandMapRepository commandMapRepository, AudioPlayerManager playerManager) {

        super(commandMapRepository);
        this.playerManager = playerManager;
    }

    @Override
    public Class<MessageCreateEvent> getEventType () {

        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute (MessageCreateEvent event) {

        final AudioPlayer player = playerManager.createPlayer();
        AudioProvider provider = new LavaPlayerAudioProvider(player);

        return Mono.just(event.getMessage())
                .filter(msg -> msg.getContent().startsWith(prefix))
                .filter(msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false))
                .map(msg -> Arrays.asList(msg.getContent().split(" ")).get(0).substring(1))
                .flatMap(cmd -> processAdminCommands(event.getMessage()).then(Mono.just(cmd)))
                .flatMap(commandMapRepository::findByCommandName)
                .flatMap(commandMap -> processCommonCommands (event, commandMap))
                .then();
    }
}
