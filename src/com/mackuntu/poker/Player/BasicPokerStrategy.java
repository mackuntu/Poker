package com.mackuntu.poker.Player;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class BasicPokerStrategy implements PlayerStrategy {
    private final Random random = new Random();
    
    @Override
    public Action decideAction(GameContext context) {
        // Convert List to ArrayList for HandEvaluator
        ArrayList<Card> allCards = new ArrayList<>(context.getHoleCards());
        allCards.addAll(context.getCommunityCards());
        
        HandEvaluator eval = new HandEvaluator(allCards);
        int handStrength = eval.getRanking();
        double potOdds = calculatePotOdds(context);
        
        // Adjust hand strength for pre-flop
        if (context.getCommunityCards().isEmpty()) {
            if (isPremiumStartingHand(context.getHoleCards())) {
                handStrength += 2;
            }
            if (isLatePosition(context.getPosition())) {
                handStrength += 1;
            }
        }
        
        // Calculate max bet based on hand strength
        int maxBet = calculateMaxBet(handStrength, context.getPlayerMoney(), context.getPosition());
        
        // Decide whether to bluff
        if (shouldBluff(context)) {
            return decideBluffAction(context, maxBet);
        }
        
        return decideNormalAction(context, handStrength, potOdds, maxBet);
    }
    
    private boolean isPremiumStartingHand(List<Card> cards) {
        if (cards.size() != 2) return false;
        Card c1 = cards.get(0);
        Card c2 = cards.get(1);
        
        // Pocket pairs
        if (c1.getNum() == c2.getNum()) {
            return c1.getNum() >= 10;  // JJ or better
        }
        
        // Suited high cards
        if (c1.getSuite() == c2.getSuite()) {
            return c1.getNum() >= 11 && c2.getNum() >= 11;  // QK suited or better
        }
        
        // High cards
        return (c1.getNum() >= 13 && c2.getNum() >= 12) ||  // AK, AQ
               (c1.getNum() >= 12 && c2.getNum() >= 13);    // AK, AQ (reverse order)
    }
    
    private boolean isLatePosition(int position) {
        return position >= 4;  // Positions 4-5 are late (in 6-max)
    }
    
    private double calculatePotOdds(GameContext context) {
        int toCall = context.getCurrentBet() - context.getCommitted();
        return (double)toCall / (context.getPotSize() + toCall);
    }
    
    private boolean shouldBluff(GameContext context) {
        double baseProb = 0.1;
        
        if (isLatePosition(context.getPosition())) {
            baseProb += 0.1;
        }
        
        if (context.getPlayerMoney() > 2000) {
            baseProb += 0.1;
        }
        
        if (context.getPlayerMoney() < 500) {
            baseProb -= 0.05;
        }
        
        return random.nextDouble() < Math.min(0.3, Math.max(0.05, baseProb));
    }
    
    private Action decideBluffAction(GameContext context, int maxBet) {
        int toCall = context.getCurrentBet() - context.getCommitted();
        
        if (isLatePosition(context.getPosition()) && toCall <= context.getPlayerMoney()/4) {
            Action raise = Action.RAISE;
            raise.setAmount(Math.min(context.getPlayerMoney()/3, maxBet));
            return raise;
        } else if (toCall == 0) {
            return Action.CHECK;
        } else if (toCall <= context.getPlayerMoney()/5) {
            return Action.CALL;
        } else {
            return Action.FOLD;
        }
    }
    
    private Action decideNormalAction(GameContext context, int handStrength, double potOdds, int maxBet) {
        int toCall = context.getCurrentBet() - context.getCommitted();
        
        if (handStrength >= 7) {  // Very strong hands
            Action raise = Action.RAISE;
            raise.setAmount(Math.min(context.getPlayerMoney(), maxBet));
            return raise;
        }
        else if (handStrength >= 5) {  // Strong hands
            if (toCall > context.getPlayerMoney()/2) {
                return Action.CALL;
            } else {
                Action raise = Action.RAISE;
                raise.setAmount(Math.min(context.getPlayerMoney()/2, maxBet));
                return raise;
            }
        }
        else if (handStrength >= 3) {  // Medium hands
            if (toCall > context.getPlayerMoney()/3) {
                return Action.FOLD;
            } else if (potOdds < 0.2) {
                return Action.CALL;
            } else {
                Action raise = Action.RAISE;
                raise.setAmount(Math.min(context.getPlayerMoney()/4, maxBet));
                return raise;
            }
        }
        else if (handStrength >= 1) {  // Weak hands
            if (toCall > context.getPlayerMoney()/4) {
                return Action.FOLD;
            } else if (toCall == 0) {
                return Action.CHECK;
            } else if (potOdds < 0.15) {
                return Action.CALL;
            } else {
                return Action.FOLD;
            }
        }
        else {  // Very weak hands
            if (toCall == 0) {
                return Action.CHECK;
            } else if (potOdds < 0.1 && isLatePosition(context.getPosition())) {
                return Action.CALL;
            } else {
                return Action.FOLD;
            }
        }
    }
    
    private int calculateMaxBet(int handStrength, int money, int position) {
        double baseBet = ((double)handStrength / 9.0) * money;
        
        if (isLatePosition(position)) {
            baseBet *= 1.2;  // 20% more aggressive in late position
        }
        
        return (int)Math.max(20, Math.min(money, baseBet));
    }
} 