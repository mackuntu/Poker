package com.mackuntu.poker.game;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Player.Player;

public class BettingManager {
    private final PlayerStateManager playerManager;
    private final BettingRules bettingRules;
    private final PotManager potManager;
    private final ActionValidator actionValidator;
    
    public BettingManager(Player[] players) {
        this.playerManager = new PokerPlayerStateManager(players);
        this.bettingRules = new StandardBettingRules();
        this.potManager = new PokerPotManager(playerManager);
        this.actionValidator = new StandardActionValidator(bettingRules);
    }
    
    public void initializeNewHand() {
        bettingRules.resetBets();
        potManager.resetPot();
        playerManager.reinitializePlayers();
    }
    
    public void initializeNewStreet() {
        bettingRules.resetBets();
        for (Integer playerIndex : playerManager.getActivePlayers()) {
            playerManager.getPlayer(playerIndex).clearCommitted();
        }
    }
    
    public boolean processAction(Action action, int playerIndex) {
        Player player = playerManager.getPlayer(playerIndex);
        if (!actionValidator.isValidAction(action, player, bettingRules.getCurrentBet())) {
            return false;
        }
        
        boolean success = false;
        int playerCommitted = player.getCommitted();
        
        switch (action) {
            case FOLD:
                playerManager.handlePlayerFold(playerIndex);
                success = true;
                break;
                
            case CHECK:
                if (bettingRules.canCheck(player)) {
                    success = true;
                }
                break;
                
            case CALL:
                success = handleCall(player);
                break;
                
            case RAISE:
                success = handleRaise(player, action.getAmount());
                break;
        }
        
        if (success) {
            player.setLastAction(action.toString());
            if (player.getMoney() <= 0) {
                playerManager.handlePlayerAllIn(playerIndex);
            }
        }
        
        return success;
    }
    
    private boolean handleCall(Player player) {
        int toCall = bettingRules.getCurrentBet() - player.getCommitted();
        if (toCall >= 0 && player.bet(toCall)) {
            potManager.addToPot(toCall);
            return true;
        }
        return false;
    }
    
    private boolean handleRaise(Player player, int raiseAmount) {
        if (!bettingRules.canRaise(player, raiseAmount)) {
            return false;
        }
        
        int totalNeeded = raiseAmount - player.getCommitted();
        if (player.bet(totalNeeded)) {
            potManager.addToPot(totalNeeded);
            bettingRules.setCurrentBet(raiseAmount);
            return true;
        }
        return false;
    }
    
    public void postBlind(int playerIndex, int amount) {
        Player player = playerManager.getPlayer(playerIndex);
        if (player.bet(amount)) {
            potManager.addToPot(amount);
            bettingRules.setCurrentBet(amount);
        }
    }
    
    public int getCurrentBet() {
        return bettingRules.getCurrentBet();
    }
    
    public int getMinRaise() {
        return bettingRules.getMinimumRaise();
    }
    
    public int getPot() {
        return potManager.getPotSize();
    }
    
    public void awardPot(int playerIndex) {
        potManager.awardPot(playerIndex);
    }
    
    public void splitPot(int[] playerIndices) {
        potManager.splitPot(playerIndices);
    }
} 