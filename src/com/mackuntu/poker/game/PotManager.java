package com.mackuntu.poker.game;

public interface PotManager {
    void addToPot(int amount);
    void awardPot(int playerIndex);
    void splitPot(int[] playerIndices);
    int getPotSize();
    void resetPot();
} 