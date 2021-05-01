package com.treta.bot.service;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import com.treta.bot.repository.CommandMapRepository;
import com.treta.bot.util.StringUtils;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HelpCommandsService {

    @Value("${bot.prefix}")
    protected String prefix;

    private final CommandMapRepository commandMapRepository;

    public Mono<CommandDTO> processHelpCommand (Message message) {

        return commandMapRepository.findAll().collectList()
                .flatMap(commands -> createHelpMessage(commands, message));
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
}
