package com.javaproject.beans;

import lombok.Data;

@Data
public class BoardGame {

    private Long id;
    private String name;
    private int level;
    private int minPlayers;
    private String maxPlayers;
    private String gameType;

    public BoardGame() {
    }

    public BoardGame(Long id, String name, int level, int minPlayers, String maxPlayers, String gameType) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public String getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(String maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}
