package com.mackuntu.poker.game;

import com.mackuntu.poker.Player.Player;

public class PokerPotManager implements PotManager {
    private int pot;
    private final PlayerStateManager playerManager;
    
    public PokerPotManager(PlayerStateManager playerManager) {
        this.playerManager = playerManager;
        resetPot();
    }
    
    @Override
    public void addToPot(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative amount to pot");
        }
        pot += amount;
    }
    
    @Override
    public void awardPot(int playerIndex) {
        if (pot <= 0) {
            throw new IllegalStateException("Cannot award zero or negative pot");
        }
        playerManager.getPlayer(playerIndex).adjustMoney(pot);
        resetPot();
    }
    
    @Override
    public void splitPot(int[] playerIndices) {
        if (pot <= 0) {
            throw new IllegalStateException("Cannot split zero or negative pot");
        }
        if (playerIndices == null || playerIndices.length == 0) {
            throw new IllegalArgumentException("Must have at least one player to split pot");
        }
        
        int share = pot / playerIndices.length;
        for (int index : playerIndices) {
            playerManager.getPlayer(index).adjustMoney(share);
        }
        
        // Handle any remainder cents
        int remainder = pot % playerIndices.length;
        if (remainder > 0) {
            playerManager.getPlayer(playerIndices[0]).adjustMoney(remainder);
        }
        
        resetPot();
    }
    
    @Override
    public int getPotSize() {
        return pot;
    }
    
    @Override
    public void resetPot() {
        pot = 0;
    }
} 