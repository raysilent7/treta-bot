package com.treta.bot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import discord4j.core.object.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document("CommandMap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandMap {

    @Id
    private String id;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime LastModifiedDate;
    private CommandType commandType;
    private Message message;
    private Map<String, String> commands;
}
