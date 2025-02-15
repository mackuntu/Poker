package com.mackuntu.poker.game;

public interface GameMessageManager {
    void addMessage(String message);
    String getLastMessage();
    void clearMessages();
} 