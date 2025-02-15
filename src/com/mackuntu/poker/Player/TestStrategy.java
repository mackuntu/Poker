package com.mackuntu.poker.Player;

import com.mackuntu.poker.Action.Action;

public class TestStrategy implements PlayerStrategy {
    private Action forcedAction;
    
    public TestStrategy() {
        this.forcedAction = null;
    }
    
    public void setForcedAction(Action action) {
        this.forcedAction = action;
    }
    
    @Override
    public Action decideAction(GameContext context) {
        if (forcedAction != null) {
            Action action = forcedAction;
            forcedAction = null;  // Clear after use
            return action;
        }
        
        // Default behavior: check if possible, fold otherwise
        return context.getCurrentBet() == context.getCommitted() ? 
            Action.CHECK : Action.FOLD;
    }
} 