package com.mackuntu.poker.Player;

import com.mackuntu.poker.Action.Action;
import java.util.Random;

public class RandomPlayerStrategy implements PlayerStrategy {
    private final Random random = new Random();
    
    @Override
    public Action decideAction(GameContext context) {
        // 25% chance each for FOLD, CHECK, CALL, RAISE
        int choice = random.nextInt(4);
        switch (choice) {
            case 0: return Action.FOLD;
            case 1: return Action.CHECK;
            case 2: return Action.CALL;
            default:
                Action raise = Action.RAISE;
                raise.setAmount(context.getCurrentBet() * 2);  // Double the current bet
                return raise;
        }
    }
} 