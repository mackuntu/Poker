package com.mackuntu.poker.Player;

import com.mackuntu.poker.Action.Action;
import java.util.Random;

public class RandomPlayerStrategy implements PlayerStrategy {
    private final Random random = new Random();
    
    @Override
    public Action decideAction(GameContext context) {
        int toCall = context.getCurrentBet() - context.getCommitted();
        
        // If there's a bet to call
        if (toCall > 0) {
            // 40% chance to call, 20% chance to raise, 40% to fold
            double rand = random.nextDouble();
            
            if (rand < 0.4 && toCall <= context.getPlayerMoney()) {
                return Action.CALL;
            } else if (rand < 0.6 && context.getPlayerMoney() > toCall * 2) {
                Action raise = Action.RAISE;
                // Raise between 2x and 3x the current bet
                int raiseAmount = context.getCurrentBet() * 2 + 
                    random.nextInt(context.getCurrentBet());
                raiseAmount = Math.min(raiseAmount, context.getPlayerMoney());
                raise.setAmount(raiseAmount);
                return raise;
            } else {
                return Action.FOLD;
            }
        }
        
        // No bet to call - can check or raise
        if (random.nextDouble() < 0.3) {
            Action raise = Action.RAISE;
            // Random raise between 2x and 4x the big blind
            int raiseAmount = Math.min(
                context.getPlayerMoney(),
                context.getCurrentBet() + (random.nextInt(3) + 2) * context.getCurrentBet()
            );
            raise.setAmount(raiseAmount);
            return raise;
        } else {
            return Action.CHECK;
        }
    }
} 