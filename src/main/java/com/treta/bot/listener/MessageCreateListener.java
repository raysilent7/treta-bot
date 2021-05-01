package com.treta.bot.listener;

import com.treta.bot.repository.CommandMapRepository;
import com.treta.bot.service.AddCommandsService;
import com.treta.bot.service.HelpCommandsService;
import com.treta.bot.service.TextCommandsService;
import com.treta.bot.service.VoiceCommandsService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class MessageCreateListener extends MessageListener implements EventListener<MessageCreateEvent> {

    public MessageCreateListener (CommandMapRepository commandMapRepository, TextCommandsService textCommandsService,
                                  AddCommandsService addCommandsService, HelpCommandsService helpCommandsService,
                                  VoiceCommandsService voiceCommandsService) {

        super(textCommandsService, helpCommandsService, addCommandsService, voiceCommandsService, commandMapRepository);
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
                .flatMap(commandMap -> processCommonCommands (event, commandMap))
                .then();
    }
}
