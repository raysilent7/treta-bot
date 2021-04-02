package com.treta.bot.DTO;

import discord4j.core.object.entity.channel.MessageChannel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommandDTO {

    MessageChannel channel;
    String commandReply;
}
