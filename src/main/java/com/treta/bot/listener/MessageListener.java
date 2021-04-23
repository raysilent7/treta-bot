package com.treta.bot.listener;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import com.treta.bot.service.AddCommandsService;
import com.treta.bot.service.HelpCommandsService;
import com.treta.bot.service.TextCommandsService;
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
    protected final CommandMapRepository commandMapRepository;

    public Mono<CommandMap> processCommonCommands (Message message, CommandMap commandMap) {
        if (CommandType.VOICE.equals(commandMap.getCommandType())) {
            return helpCommandsService.helpCommand(message)
                    .flatMap(this::replyCommand);
        }
        else {
            return textCommandsService.processTextCommand(message, commandMap)
                    .flatMap(this::replyCommand);
        }
    }

    public Mono<CommandMap> processAdminCommands (Message message) {
        String commandName = Arrays.asList(message.getContent().split(" ")).get(0).substring(1);
        if (message.getAuthor().map(User::isBot).orElse(false)) {
            return Mono.empty();
        }
        else if (AdminCommands.ADD.getName().equals(commandName)) {
            return addCommandsService.addNewTextCommand(message)
                    .flatMap(this::replyCommand);
        }
        else if (AdminCommands.HELP.getName().equals(commandName)) {
            return helpCommandsService.helpCommand(message)
                    .flatMap(this::replyCommand);
        }
        return Mono.empty();
    }

    private Mono<CommandMap> replyCommand (CommandDTO commandDTO) {

        return Mono.just(commandDTO)
                .flatMap(dto -> dto.getMessage().getChannel())
                .flatMap(channel -> reply(channel, commandDTO))
                .then(Mono.just(commandDTO.getCommandMap()));
    }

    private Mono<Message> reply (MessageChannel channel, CommandDTO commandDTO) {

        if (AdminCommands.ADD.equals(commandDTO.getAdminCommand())) {
            return channel.createMessage(commandDTO.returnSuccessReply());
        }
        else {
            return channel.createMessage(commandDTO.getCommandMap().getCommandReply());
        }
    }
}
