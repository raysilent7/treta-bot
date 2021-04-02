package com.treta.bot.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Map;

public interface CommandMapRepository extends ReactiveMongoRepository<Map<String, String>, String> {
}
