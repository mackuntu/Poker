package com.mackuntu.poker.game;

public enum GameState {
    START("Starting new hand"),
    FLOP("Dealing flop"),
    TURN("Dealing turn"),
    RIVER("Dealing river"),
    FINISH("Showing results"),
    WAITING("Waiting for action");

    private final String message;

    GameState(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
} 