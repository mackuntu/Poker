package com.mackuntu.poker.game;

import java.util.ArrayList;
import java.util.List;

public class StandardGameMessageManager implements GameMessageManager {
    private final List<String> messages;
    
    public StandardGameMessageManager() {
        this.messages = new ArrayList<>();
    }
    
    @Override
    public void addMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            messages.add(message);
        }
    }
    
    @Override
    public String getLastMessage() {
        return messages.isEmpty() ? "" : messages.get(messages.size() - 1);
    }
    
    @Override
    public void clearMessages() {
        messages.clear();
    }
} 