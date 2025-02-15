package com.mackuntu.poker.game;

import java.util.ArrayList;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Dealer.Dealer;
import com.mackuntu.poker.Player.Player;

public class CardManager {
    private final ArrayList<Card> communityCards;
    private final ArrayList<Card> burnCards;
    private final Player[] players;
    private final boolean testMode;
    private Dealer dealer;
    
    public CardManager(Player[] players) {
        this(players, false);
    }
    
    public CardManager(Player[] players, boolean testMode) {
        this.players = players;
        this.testMode = testMode;
        this.communityCards = new ArrayList<>(5);
        this.burnCards = new ArrayList<>(3);
    }
    
    public void initializeNewHand() {
        dealer = new Dealer(testMode);
        communityCards.clear();
        burnCards.clear();
    }
    
    private Card createCard(int cardIndex) {
        int suite = cardIndex / 13;
        int rank = (cardIndex % 13) + 1;
        return new Card(rank, suite);
    }
    
    public void dealInitialCards(int dealerPosition) {
        // Deal cards to each player
        for (int i = 0; i < players.length * 2; i++) {
            int targetPlayer = (dealerPosition + 1 + i) % players.length;
            if (players[targetPlayer].getMoney() > 0) {
                players[targetPlayer].addCard(createCard(dealer.getCard()));
            }
        }
        
        // Initialize hand evaluators
        for (Player player : players) {
            if (player.getMoney() > 0) {
                player.addCard(createCard(dealer.getCard()));
            }
        }
    }
    
    public void dealNextStreet(GameState gameState) {
        switch (gameState) {
            case FLOP:
                dealFlop();
                break;
            case TURN:
            case RIVER:
                dealTurnOrRiver();
                break;
            default:
                break;
        }
    }
    
    private void dealFlop() {
        burnCards.add(createCard(dealer.getCard()));
        for (int i = 0; i < 3; i++) {
            Card card = createCard(dealer.getCard());
            communityCards.add(card);
            addCardToActivePlayers(card);
        }
    }
    
    private void dealTurnOrRiver() {
        burnCards.add(createCard(dealer.getCard()));
        Card card = createCard(dealer.getCard());
        communityCards.add(card);
        addCardToActivePlayers(card);
    }
    
    private void addCardToActivePlayers(Card card) {
        for (Player player : players) {
            if (!player.isFolded()) {
                player.addCard(card);
            }
        }
    }
    
    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }
} 