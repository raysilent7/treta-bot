package com.treta.bot.listener;

import com.treta.bot.DTO.CommandDTO;
import com.treta.bot.repository.CommandMapRepository;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class MessageListener {

    protected final CommandMapRepository commandMapRepository;

    public Mono<Void> processTextCommand (Message eventMessage) {

        return Mono.just(eventMessage)
                .flatMap(this::resolveCommand)
                .flatMap(Message::getChannel)
                .flatMap(channel -> retrieveCommand(channel, eventMessage))
                .flatMap(commandDTO -> commandDTO.getChannel().createMessage(commandDTO.getCommandReply()))
                .then();
    }

    protected Mono<Message> resolveCommand (Message message) {

        return commandMapRepository.findAll()
                .collectList()
                .flatMap(list -> resolveName(list, message))
                .filter(msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false));
    }

    protected Mono<CommandDTO> retrieveCommand (MessageChannel channel, Message message) {

        return commandMapRepository.findAll()
                .collectList()
                .map(list -> list.get(0).get(message.getContent().substring(1)))
                .map(command -> CommandDTO.builder()
                        .commandReply(command)
                        .channel(channel)
                        .build());
    }

    private Mono<Message> resolveName (List<Map<String, String>> commands, Message message) {

        if (commands.get(0).containsKey(message.getContent().substring(1))) {
            return Mono.just(message);
        }
        else {
            return Mono.empty();
        }
    }
}
