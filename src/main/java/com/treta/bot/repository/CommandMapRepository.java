package com.treta.bot.repository;

import com.treta.bot.domain.CommandMap;
import com.treta.bot.domain.CommandType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CommandMapRepository extends ReactiveMongoRepository<CommandMap, String> {

    Mono<CommandMap> findByCommandName (String name);
}
