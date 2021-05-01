package com.treta.bot.domain;

import lombok.Getter;

public enum AdminCommands {

    HELP("help", "mostra os comandos existentes no bot."),
    ADD("addCommand", "Adiciona novos comandos de texto. Ex.: addCommand `nome` `resposta`"),
    ADD_VOICE("addMeme", "Adiciona novos comandos de voz. Ex.: addCommand `nome` `link do youtube`"),
    REMOVE("removeCommand", "Remove o comando de texto escpecificado. Ex.: removeCommand `nome`"),
    NORMAL("anyCommand", "comandos que não são admin");

    @Getter
    private String name;

    @Getter
    private String description;

    AdminCommands (String name, String description) {

        this.name = name;
        this.description = description;
    }

    public static String returnFullDescription () {
        return "$" + AdminCommands.HELP.getName() + ": " + AdminCommands.HELP.getDescription() + "\n"
                + "$" + AdminCommands.ADD.getName() + ": " + AdminCommands.ADD.getDescription() + "\n"
                + "$" + AdminCommands.ADD_VOICE.getName() + ": " + AdminCommands.ADD_VOICE.getDescription() + "\n"
                + "$" + AdminCommands.REMOVE.getName() + ": " + AdminCommands.REMOVE.getDescription() + "\n";
    }
}
