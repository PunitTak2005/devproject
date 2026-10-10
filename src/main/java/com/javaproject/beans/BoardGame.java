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
}
