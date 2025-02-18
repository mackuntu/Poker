package com.mackuntu.poker.game;

import com.mackuntu.poker.Player.Player;

public class StandardBettingRules implements BettingRules {
    private int currentBet;
    private int minRaise;
    
    public StandardBettingRules() {
        resetBets();
    }
    
    @Override
    public boolean canCheck(Player player) {
        // Can only check if player has matched the current bet
        // AND there is no current bet to call
        return player.getCommitted() == currentBet && currentBet == 0;
    }
    
    @Override
    public boolean canCall(Player player) {
        int toCall = currentBet - player.getCommitted();
        // Can call if there's a bet to match and player has enough money
        return toCall > 0 && toCall <= player.getMoney();
    }
    
    @Override
    public boolean canRaise(Player player, int amount) {
        if (amount <= currentBet) {
            return false;
        }
        
        int totalNeeded = amount - player.getCommitted();
        // Can raise if:
        // 1. Amount is more than current bet
        // 2. Player has enough money
        // 3. Raise amount meets minimum raise requirement
        return totalNeeded > 0 && 
               totalNeeded <= player.getMoney() && 
               (amount - currentBet) >= minRaise;
    }
    
    @Override
    public int getMinimumRaise() {
        return minRaise;
    }
    
    @Override
    public int getCurrentBet() {
        return currentBet;
    }
    
    @Override
    public void setCurrentBet(int amount) {
        this.currentBet = amount;
        this.minRaise = amount; // Minimum raise is typically the size of the current bet
    }
    
    @Override
    public void resetBets() {
        currentBet = 0;
        minRaise = 0;
    }
} 