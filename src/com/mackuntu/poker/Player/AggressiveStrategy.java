package com.mackuntu.poker.Player;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import java.util.ArrayList;
import java.util.List;

public class AggressiveStrategy implements PlayerStrategy {
    @Override
    public Action decideAction(GameContext context) {
        // Convert List to ArrayList for HandEvaluator
        ArrayList<Card> allCards = new ArrayList<>(context.getHoleCards());
        allCards.addAll(context.getCommunityCards());
        
        HandEvaluator eval = new HandEvaluator(allCards);
        int handStrength = eval.getRanking();
        
        // More aggressive pre-flop play
        if (context.getCommunityCards().isEmpty()) {
            return handlePreFlop(context, handStrength);
        }
        
        return handlePostFlop(context, handStrength);
    }
    
    private Action handlePreFlop(GameContext context, int handStrength) {
        int toCall = context.getCurrentBet() - context.getCommitted();
        
        // Always raise with strong hands
        if (handStrength >= 3 || isPremiumStartingHand(context.getHoleCards())) {
            Action raise = Action.RAISE;
            raise.setAmount(Math.min(context.getPlayerMoney(), 
                context.getCurrentBet() * 3));  // 3x raise
            return raise;
        }
        
        // Call with medium strength hands
        if (handStrength >= 1 && toCall <= context.getPlayerMoney() / 3) {
            return Action.CALL;
        }
        
        // 50% chance to raise with any hand in late position
        if (isLatePosition(context.getPosition()) && Math.random() > 0.5) {
            Action raise = Action.RAISE;
            raise.setAmount(Math.min(context.getPlayerMoney(), 
                context.getCurrentBet() * 2));  // 2x raise
            return raise;
        }
        
        // Check if possible, otherwise fold
        return toCall == 0 ? Action.CHECK : Action.FOLD;
    }
    
    private Action handlePostFlop(GameContext context, int handStrength) {
        int toCall = context.getCurrentBet() - context.getCommitted();
        
        // Always raise with strong hands
        if (handStrength >= 4) {
            Action raise = Action.RAISE;
            raise.setAmount(Math.min(context.getPlayerMoney(), 
                context.getPotSize()));  // Pot-sized bet
            return raise;
        }
        
        // Call with medium strength hands
        if (handStrength >= 2 && toCall <= context.getPlayerMoney() / 2) {
            return Action.CALL;
        }
        
        // 30% chance to bluff raise
        if (Math.random() > 0.7) {
            Action raise = Action.RAISE;
            raise.setAmount(Math.min(context.getPlayerMoney(), 
                context.getPotSize() / 2));  // Half pot bet
            return raise;
        }
        
        // Check if possible, otherwise fold
        return toCall == 0 ? Action.CHECK : Action.FOLD;
    }
    
    private boolean isPremiumStartingHand(List<Card> cards) {
        if (cards.size() != 2) return false;
        Card c1 = cards.get(0);
        Card c2 = cards.get(1);
        
        // More hands considered premium in aggressive strategy
        // Pocket pairs
        if (c1.getNum() == c2.getNum()) {
            return c1.getNum() >= 8;  // 88 or better
        }
        
        // Suited cards
        if (c1.getSuite() == c2.getSuite()) {
            return c1.getNum() >= 10 && c2.getNum() >= 10;  // Any suited 10+ cards
        }
        
        // High cards
        return (c1.getNum() >= 12 && c2.getNum() >= 11) ||  // AK, AQ, AJ, KQ
               (c1.getNum() >= 11 && c2.getNum() >= 12);    // Same in reverse order
    }
    
    private boolean isLatePosition(int position) {
        return position >= 4;  // Positions 4-5 are late (in 6-max)
    }
} 