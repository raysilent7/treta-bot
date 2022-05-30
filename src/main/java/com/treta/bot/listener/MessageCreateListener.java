package com.treta.bot.listener;

import com.treta.bot.domain.CommandMap;
import com.treta.bot.repository.CommandMapRepository;
import com.treta.bot.service.AddCommandsService;
import com.treta.bot.service.HelpCommandsService;
import com.treta.bot.service.RemoveCommandsService;
import com.treta.bot.service.TextCommandsService;
import com.treta.bot.service.VoiceCommandsService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
@Slf4j
public class MessageCreateListener extends MessageListener implements EventListener<MessageCreateEvent> {

    public MessageCreateListener (CommandMapRepository commandMapRepository, TextCommandsService textCommandsService,
                                  AddCommandsService addCommandsService, HelpCommandsService helpCommandsService,
                                  RemoveCommandsService removeCommandsService, VoiceCommandsService voiceCommandsService) {

        super(textCommandsService, helpCommandsService, addCommandsService, voiceCommandsService, removeCommandsService, commandMapRepository);
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
                .map(msg -> Arrays.asList(msg.getContent().split(" ")).get(0).substring(1))
                .flatMap(cmd -> processAdminCommands(event.getMessage()).then(Mono.just(cmd)))
                .flatMap(commandMapRepository::findByCommandName)
                .flatMap(commandMap -> processCommonCommands(event, commandMap))
                .onErrorResume(e -> {
                    log.error(e.getMessage(), e);
                    return Mono.just(CommandMap.builder().build());
                })
                .then();
    }
}
