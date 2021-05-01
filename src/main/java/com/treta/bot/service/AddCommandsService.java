package com.treta.bot.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.config.TrackScheduler;
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

    private final CommandMapRepository commandMapRepository;
    private final AudioPlayerManager playerManager;

    public Mono<CommandDTO> addNewTextCommand (Message message) {

        return buildCommandMap(CommandType.TEXT, message)
                .flatMap(commandMap -> commandMapRepository.findByCommandName(commandMap.getCommandName()))
                .switchIfEmpty(buildCommandMap(CommandType.TEXT, message))
                .flatMap(commandMap -> resolveArgs(commandMap, message))
                .flatMap(this::saveNewCommand);
    }

    public Mono<CommandDTO> addNewVoiceCommand (Message message) {

        return buildCommandMap(CommandType.VOICE, message)
                .flatMap(commandMap -> commandMapRepository.findByCommandName(commandMap.getCommandName()))
                .switchIfEmpty(buildCommandMap(CommandType.VOICE, message))
                .flatMap(commandMap -> resolveArgs(commandMap, message))
                .flatMap(this::resolveTrackDurationAndSave);
    }

    private Mono<CommandDTO> resolveTrackDurationAndSave (CommandDTO commandDTO) {

        return Mono.just(commandDTO.getCommandMap())
                .map(map -> playerManager.loadItem(map.getCommandReply(), new TrackScheduler() {

                    @Override
                    public void trackLoaded (AudioTrack track) {
                        Mono.just(commandDTO)
                                .flatMap(dto -> {
                                    commandDTO.getCommandMap().setDuration(track.getDuration() + 1500);
                                    return Mono.just(dto);
                                })
                                .flatMap(dto -> commandMapRepository.save(dto.getCommandMap())
                                        .thenReturn(commandDTO)).block();
                    }
                }))
                .thenReturn(commandDTO);
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
        commandDTO.setAdminCommand(AdminCommands.ADD_TEXT);
        return commandMapRepository.save(commandDTO.getCommandMap())
                .thenReturn(commandDTO);
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
}
