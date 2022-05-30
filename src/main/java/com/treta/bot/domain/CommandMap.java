package com.treta.bot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document("command-map")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandMap {

    @Id
    private String id;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime LastModifiedDate;
    private CommandType commandType;
    private long duration;
    @Indexed(unique = true, background = true)
    private String commandName;
    private String commandDescription;
    private String commandReply;

    public void resolveReply (List<String> args) {

        args.remove(0);
        args.remove(0);
        this.commandReply = String.join(" ", args);
    }
}
