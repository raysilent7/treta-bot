package com.treta.bot.listener;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import com.treta.bot.service.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RequiredArgsConstructor
public abstract class MessageListener {

    @Value("${bot.prefix}")
    protected String prefix;

    private final TextCommandsService textCommandsService;
    private final HelpCommandsService helpCommandsService;
    private final AddCommandsService addCommandsService;
    private final VoiceCommandsService voiceCommandsService;
    private final RemoveCommandsService removeCommandsService;
    protected final CommandMapRepository commandMapRepository;

    public Mono<CommandMap> processCommonCommands (MessageCreateEvent event, CommandMap commandMap) {

        if (CommandType.VOICE.equals(commandMap.getCommandType())) {
            return voiceCommandsService.processVoiceCommand(event, commandMap)
                    .flatMap(this::replyCommand);
        }
        else {
            return textCommandsService.processTextCommand(event.getMessage(), commandMap)
                    .flatMap(this::replyCommand);
        }
    }

    public Mono<CommandMap> processAdminCommands (Message message) {

        String commandName = Arrays.asList(message.getContent().split(" ")).get(0).substring(1);

        if (message.getAuthor().map(User::isBot).orElse(false)) {
            return Mono.empty();
        }
        else if (AdminCommands.ADD_TEXT.getName().equals(commandName)) {
            return addCommandsService.addNewTextCommand(message)
                    .flatMap(this::replyCommand);
        }
        else if (AdminCommands.HELP.getName().equals(commandName)) {
            return helpCommandsService.processHelpCommand(message)
                    .flatMap(this::replyCommand);
        }
        else if (AdminCommands.MEMES.getName().equals(commandName)) {
            return helpCommandsService.processListMemesCommand(message)
                    .flatMap(this::replyCommand);
        }
        else if (AdminCommands.ADD_VOICE.getName().equals(commandName)) {
            return addCommandsService.addNewVoiceCommand(message)
                    .flatMap(this::replyCommand);
        }
        else if (AdminCommands.REMOVE.getName().equals(commandName)) {
            return removeCommandsService.processRemoveCommand(message)
                    .flatMap(this::replyCommand);
        }
        return Mono.empty();
    }

    private Mono<CommandMap> replyCommand (CommandDTO commandDTO) {

        return Mono.just(commandDTO)
                .flatMap(dto -> dto.getMessage().getChannel())
                .flatMap(channel -> reply(channel, commandDTO))
                .thenReturn(commandDTO.getCommandMap());
    }

    private Mono<Message> reply (MessageChannel channel, CommandDTO commandDTO) {

        if (AdminCommands.ADD_TEXT.equals(commandDTO.getAdminCommand())) {
            return channel.createMessage(commandDTO.returnSuccessReply());
        }
        else {
            return channel.createMessage(commandDTO.getCommandMap().getCommandReply());
        }
    }
}
