package com.treta.bot.repository;

import com.treta.bot.domain.CommandMap;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CommandMapRepository extends ReactiveMongoRepository<CommandMap, String> {

    Mono<CommandMap> findByCommandName (String name);

    @DeleteQuery(value="{'commandName' : $0}")
    Mono<CommandMap> deleteByCommandName (String name);
}
