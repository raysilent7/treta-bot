package com.treta.bot.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.config.LavaPlayerAudioProvider;
import com.treta.bot.config.TrackScheduler;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class VoiceCommandsService {

    private final AudioPlayerManager playerManager;

    public Mono<CommandDTO> processVoiceCommand (MessageCreateEvent event, CommandMap commandMap) {

        final AudioPlayer player = playerManager.createPlayer();
        AudioProvider provider = new LavaPlayerAudioProvider(player);
        TrackScheduler scheduler = new TrackScheduler(player);

        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
                .then(Mono.just(commandMap))
                .map(cmdMap -> playerManager.loadItem(cmdMap.getCommandReply(), scheduler))
                .then(endVoiceConnection(event, commandMap));
    }

    private Mono<CommandDTO> endVoiceConnection (MessageCreateEvent event, CommandMap commandMap) {

        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(VoiceChannel::getVoiceConnection)
                .delaySubscription(Duration.ofMillis(commandMap.getDuration()))
                .flatMap(VoiceConnection::disconnect)
                .then(Mono.just(CommandDTO.builder()
                        .commandType(CommandType.VOICE)
                        .adminCommand(AdminCommands.NORMAL)
                        .commandMap(commandMap)
                        .message(event.getMessage())
                        .build()));
    }
}
