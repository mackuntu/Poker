package com.mackuntu.poker.game;

public class StandardDealerManager implements DealerPositionManager {
    private int dealerPosition;
    private final int numPlayers;
    
    public StandardDealerManager(int numPlayers) {
        this.numPlayers = numPlayers;
        this.dealerPosition = 0;
    }
    
    @Override
    public int getDealerPosition() {
        return dealerPosition;
    }
    
    @Override
    public void setDealerPosition(int position) {
        if (position < 0 || position >= numPlayers) {
            throw new IllegalArgumentException("Invalid dealer position");
        }
        this.dealerPosition = position;
    }
    
    @Override
    public void advanceDealer(PlayerStateManager playerManager) {
        do {
            dealerPosition = (dealerPosition + 1) % numPlayers;
        } while (!playerManager.isPlayerActive(dealerPosition));
    }
} 