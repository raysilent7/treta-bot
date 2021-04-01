package com.treta.bot.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class BotConfiguration {


    @Value("${bot.token}")
    private String token;

    @Bean
    @PostConstruct
    public GatewayDiscordClient gatewayDiscordClient() {

        return DiscordClientBuilder.create(token).build().login().block();
    }
}
