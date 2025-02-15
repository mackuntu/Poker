package com.mackuntu.poker.game;

import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Action.Action;

public class BettingManager {
    private final Player[] players;
    private final GameStateManager gameStateManager;
    private int currentBet;  // Current bet to call
    private int pot;        // Total pot size
    private int minRaise;   // Minimum raise amount
    
    public BettingManager(Player[] players, GameStateManager gameStateManager) {
        this.players = players;
        this.gameStateManager = gameStateManager;
        this.currentBet = 0;
        this.pot = 0;
        this.minRaise = 0;
    }
    
    public void initializeNewHand() {
        currentBet = 0;
        pot = 0;
        minRaise = 0;
        for (Player player : players) {
            if (player.isActive()) {
                player.clearCommitted();
            }
        }
    }
    
    public boolean processAction(Action action, int playerIndex) {
        Player player = players[playerIndex];
        if (!player.canAct()) {
            return false;
        }
        
        boolean success = false;
        int playerCommitted = player.getCommitted();
        
        switch (action) {
            case FOLD:
                player.fold();
                gameStateManager.handlePlayerAction(playerIndex, "FOLD");
                success = true;
                break;
                
            case CHECK:
                // Can only check if no bet to call
                if (playerCommitted == currentBet) {
                    success = true;
                }
                break;
                
            case CALL:
                // Must match the current bet exactly
                int toCall = currentBet - playerCommitted;
                if (toCall >= 0 && player.bet(toCall)) {
                    pot += toCall;
                    success = true;
                }
                break;
                
            case RAISE:
                // Must be at least minimum raise
                int raiseAmount = action.getAmount();
                if (raiseAmount <= currentBet) {
                    return false;
                }
                
                // Calculate total amount needed
                int totalNeeded = raiseAmount - playerCommitted;
                if (totalNeeded > 0 && player.bet(totalNeeded)) {
                    pot += totalNeeded;
                    currentBet = raiseAmount;
                    minRaise = raiseAmount - currentBet; // Set new minimum raise
                    success = true;
                }
                break;
                
            default:
                break;
        }
        
        if (success) {
            if (player.hasNoMoney()) {
                gameStateManager.handleMoneyChange(playerIndex);
            }
            player.setLastAction(action.toString());
        }
        
        return success;
    }
    
    public void postBlind(int playerIndex, int amount) {
        Player player = players[playerIndex];
        if (player.bet(amount)) {
            pot += amount;
            currentBet = amount;
            minRaise = amount; // Minimum raise is the size of the big blind
            if (player.hasNoMoney()) {
                gameStateManager.handleMoneyChange(playerIndex);
            }
        }
    }
    
    public int getCurrentBet() {
        return currentBet;
    }
    
    public int getMinRaise() {
        return minRaise;
    }
    
    public int getPot() {
        return pot;
    }
    
    public void awardPot(int playerIndex) {
        players[playerIndex].adjustMoney(pot);
        pot = 0;
    }
    
    public void splitPot(int[] playerIndices) {
        int share = pot / playerIndices.length;
        for (int index : playerIndices) {
            players[index].adjustMoney(share);
        }
        pot = 0;
    }
} 