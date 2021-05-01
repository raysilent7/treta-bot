package com.treta.bot.domain;

import lombok.Getter;

public enum AdminCommands {

    HELP("help", "mostra os comandos existentes no bot."),
    ADD_TEXT("addText", "Adiciona novos comandos de texto. Ex.: addText `nome` `resposta`"),
    ADD_VOICE("addMeme", "Adiciona novos comandos de voz. Ex.: addMeme `nome` `link do video/audio`"),
    REMOVE("remove", "Remove o comando escpecificado. Ex.: remove `nome`"),
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
                + "$" + AdminCommands.ADD_TEXT.getName() + ": " + AdminCommands.ADD_TEXT.getDescription() + "\n"
                + "$" + AdminCommands.ADD_VOICE.getName() + ": " + AdminCommands.ADD_VOICE.getDescription() + "\n"
                + "$" + AdminCommands.REMOVE.getName() + ": " + AdminCommands.REMOVE.getDescription() + "\n";
    }
}
