package com.mackuntu.poker.game;

public interface DealerPositionManager {
    int getDealerPosition();
    void setDealerPosition(int position);
    void advanceDealer(PlayerStateManager playerManager);
} 