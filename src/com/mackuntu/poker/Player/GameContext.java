package com.mackuntu.poker.Player;

import com.mackuntu.poker.Card.Card;
import java.util.List;
import java.util.ArrayList;

/**
 * Immutable context object containing all information needed for decision making
 */
public class GameContext {
    private final List<Card> holeCards;
    private final List<Card> communityCards;
    private final int currentBet;
    private final int playerMoney;
    private final int committed;
    private final int position;
    private final int potSize;
    
    private GameContext(Builder builder) {
        this.holeCards = List.copyOf(builder.holeCards);
        this.communityCards = List.copyOf(builder.communityCards);
        this.currentBet = builder.currentBet;
        this.playerMoney = builder.playerMoney;
        this.committed = builder.committed;
        this.position = builder.position;
        this.potSize = builder.potSize;
    }
    
    // Getters
    public List<Card> getHoleCards() { return holeCards; }
    public List<Card> getCommunityCards() { return communityCards; }
    public int getCurrentBet() { return currentBet; }
    public int getPlayerMoney() { return playerMoney; }
    public int getCommitted() { return committed; }
    public int getPosition() { return position; }
    public int getPotSize() { return potSize; }
    
    public static class Builder {
        private List<Card> holeCards = new ArrayList<>();
        private List<Card> communityCards = new ArrayList<>();
        private int currentBet;
        private int playerMoney;
        private int committed;
        private int position;
        private int potSize;
        
        public Builder holeCards(List<Card> holeCards) {
            this.holeCards = holeCards;
            return this;
        }
        
        public Builder communityCards(List<Card> communityCards) {
            this.communityCards = communityCards;
            return this;
        }
        
        public Builder currentBet(int currentBet) {
            this.currentBet = currentBet;
            return this;
        }
        
        public Builder playerMoney(int playerMoney) {
            this.playerMoney = playerMoney;
            return this;
        }
        
        public Builder committed(int committed) {
            this.committed = committed;
            return this;
        }
        
        public Builder position(int position) {
            if (position < 0 || position > 5) {
                throw new IllegalArgumentException("Position must be between 0 and 5");
            }
            this.position = position;
            return this;
        }
        
        public Builder potSize(int potSize) {
            this.potSize = potSize;
            return this;
        }
        
        public GameContext build() {
            return new GameContext(this);
        }
    }
} 