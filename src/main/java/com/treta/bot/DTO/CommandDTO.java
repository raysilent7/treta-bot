package com.treta.bot.DTO;

import com.treta.bot.domain.AdminCommands;
import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import discord4j.core.object.entity.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommandDTO {

    private Message message;
    private CommandMap commandMap;
    private CommandType commandType;
    private AdminCommands adminCommand;

    public String returnSuccessReply () {
        return "Comando adicionado com sucesso: " + this.commandMap.getCommandName();
    }
}
