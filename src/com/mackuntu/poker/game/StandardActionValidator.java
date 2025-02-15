package com.mackuntu.poker.game;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Player.Player;

public class StandardActionValidator implements ActionValidator {
    private final BettingRules bettingRules;
    
    public StandardActionValidator(BettingRules bettingRules) {
        this.bettingRules = bettingRules;
    }
    
    @Override
    public boolean isValidAction(Action action, Player player, int currentBet) {
        if (!player.canAct()) {
            return false;
        }
        
        switch (action) {
            case FOLD:
                return true;
                
            case CHECK:
                return bettingRules.canCheck(player);
                
            case CALL:
                return bettingRules.canCall(player);
                
            case RAISE:
                return bettingRules.canRaise(player, action.getAmount());
                
            default:
                return false;
        }
    }
} 