package com.treta.bot.listener;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import com.treta.bot.util.StringUtils;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public abstract class MessageListener {

    @Value("${bot.prefix}")
    protected String prefix;

    public final CommandMapRepository commandMapRepository;

    public Mono<CommandMap> processCommonCommands (MessageCreateEvent event, CommandMap commandMap) {
        if (CommandType.VOICE.equals(commandMap.getCommandType())) {
            return helpCommand(event.getMessage());
        }
        else {
            return processTextCommand(event.getMessage(), commandMap);
        }
    }

    public Mono<CommandMap> processAdminCommands (Message message) {
        String commandName = Arrays.asList(message.getContent().split(" ")).get(0).substring(1);
        if (message.getAuthor().map(User::isBot).orElse(false)) {
            return Mono.empty();
        }
        else if (AdminCommands.ADD.getName().equals(commandName)) {
            return addNewTextCommand(message);
        }
        else if (AdminCommands.HELP.getName().equals(commandName)) {
            return helpCommand(message);
        }
        return Mono.empty();
    }

    public Mono<CommandMap> processTextCommand (Message message, CommandMap commandMap) {

        return resolveCommand(message, commandMap)
                .flatMap(this::replyCommand);
    }

    public Mono<CommandMap> addNewTextCommand (Message message) {

        return buildCommandMap(CommandType.TEXT, message)
                .flatMap(commandMap -> commandMapRepository.findByCommandName(commandMap.getCommandName()))
                .switchIfEmpty(buildCommandMap(CommandType.TEXT, message))
                .flatMap(commandMap -> resolveArgs(commandMap, message))
                .flatMap(this::saveNewCommand)
                .flatMap(this::replyCommand);
    }

    public Mono<CommandMap> helpCommand (Message message) {

        return commandMapRepository.findAll().collectList()
                .flatMap(commands -> createHelpMessage(commands, message))
                .flatMap(this::replyCommand);
    }

    private Mono<CommandDTO> createHelpMessage (List<CommandMap> commands, Message message) {

        String helpMessage = "__**Comandos**__\n\n" + AdminCommands.returnFullDescription();

        for (CommandMap map : commands) {
            helpMessage = helpMessage.concat(prefix + map.getCommandName() + StringUtils.nullIsBlank(map.getCommandDescription()) + "\n");
        }

        CommandMap commandMap = CommandMap.builder()
                .commandName(AdminCommands.HELP.getName())
                .LastModifiedDate(LocalDateTime.now())
                .commandType(CommandType.ADMIN)
                .commandReply(helpMessage)
                .build();

        return Mono.just(CommandDTO.builder()
                .commandMap(commandMap)
                .message(message)
                .commandType(CommandType.ADMIN)
                .adminCommand(AdminCommands.HELP)
                .build());
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
        commandDTO.setAdminCommand(AdminCommands.ADD);
        return commandMapRepository.save(commandDTO.getCommandMap())
                .then(Mono.just(commandDTO));
    }

    private Mono<CommandDTO> resolveArgs (CommandMap commandMap, Message message) {

        List<String> args = new LinkedList<>(Arrays.asList(message.getContent().split(" ")));
        commandMap.setCommandName(args.get(1));
        commandMap.setCommandDescription(args.get(2));
        commandMap.resolveReply(args);

        return Mono.just(CommandDTO.builder()
                .commandMap(commandMap)
                .message(message)
                .build());
    }

    private Mono<CommandDTO> resolveCommand (Message message, CommandMap commandMap) {

        return Mono.just(CommandDTO.builder()
                        .commandType(CommandType.TEXT)
                        .adminCommand(AdminCommands.NORMAL)
                        .commandMap(commandMap)
                        .message(message)
                        .build());
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
