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

        return createHelpMessage()
                .flatMap(msgToSend -> buildCommandDTO(AdminCommands.HELP, msgToSend, message));
    }

    public Mono<CommandDTO> processListMemesCommand (Message message) {

        return commandMapRepository.findAll().collectList()
                .flatMap(this::createListMemeMessage)
                .flatMap(msgToSend -> buildCommandDTO(AdminCommands.MEMES, msgToSend, message));
    }

    private Mono<CommandDTO> buildCommandDTO (AdminCommands admin, String msgToSend, Message message) {

        CommandMap commandMap = CommandMap.builder()
                .commandName(admin.getName())
                .LastModifiedDate(LocalDateTime.now())
                .commandType(CommandType.ADMIN)
                .commandReply(msgToSend)
                .build();

        return Mono.just(CommandDTO.builder()
                .commandMap(commandMap)
                .message(message)
                .commandType(CommandType.ADMIN)
                .adminCommand(admin)
                .build());
    }

    private Mono<String> createHelpMessage () {

        return Mono.just("__**Comandos**__\n\n" + AdminCommands.returnFullDescription());
    }

    private Mono<String> createListMemeMessage (List<CommandMap> commands) {

        String msgToSend = "__**Memes**__\n\n";

        for (CommandMap map : commands) {
            msgToSend = msgToSend.concat(prefix + map.getCommandName() + StringUtils.nullIsBlank(map.getCommandDescription()) + "\n");
        }

        return Mono.just(msgToSend);
    }
}
