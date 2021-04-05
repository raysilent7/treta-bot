package com.treta.bot.DTO;

import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import discord4j.core.object.entity.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommandDTO {

    private Message message;
    private CommandMap commandMap;
    private CommandType commandType;
    private List<String> args;
}
