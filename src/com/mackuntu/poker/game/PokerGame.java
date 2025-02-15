package com.mackuntu.poker.game;

import java.util.ArrayList;
import java.util.List;
import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.game.GameState;
import com.mackuntu.poker.Evaluator.HandEvaluator;

public class PokerGame {
    private final Player[] players;
    private final GameStateManager stateManager;
    private final BettingManager bettingManager;
    private final CardManager cardManager;
    private final ArrayList<String> handAnalysis;
    private final boolean testMode;
    private int currentPlayer;
    private int smallBlind;
    private int bigBlind;
    private int handsPlayed;
    private int roundStartPlayer = -1;
    
    public PokerGame(Player[] players, boolean testMode) {
        this.players = players;
        this.testMode = testMode;
        this.stateManager = new GameStateManager(players);
        this.smallBlind = 20;
        this.bigBlind = 40;
        this.bettingManager = new BettingManager(players, stateManager);
        this.cardManager = new CardManager(players, testMode);
        this.handAnalysis = new ArrayList<>();
        this.handsPlayed = 0;
        
        // Initialize player positions
        for (int i = 0; i < players.length; i++) {
            players[i].setMoney(1000);  // Starting stack
        }
    }
    
    public void startNewHand() {
        handsPlayed++;
        if (handsPlayed % 10 == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            addHandAnalysis("Blinds increased to " + smallBlind + "/" + bigBlind);
        }
        
        // Reset game state
        stateManager.initializeNewHand();
        bettingManager.initializeNewHand();
        cardManager.initializeNewHand();
        handAnalysis.clear();
        
        // Post blinds
        int smallBlindPos = (stateManager.getDealerIndex() + 1) % players.length;
        int bigBlindPos = (stateManager.getDealerIndex() + 2) % players.length;
        
        bettingManager.postBlind(smallBlindPos, smallBlind);
        bettingManager.postBlind(bigBlindPos, bigBlind);
        
        // Deal cards
        cardManager.dealInitialCards(stateManager.getDealerIndex());
        
        // Set initial player
        currentPlayer = (stateManager.getDealerIndex() + 3) % players.length;
        while (!stateManager.isPlayerActive(currentPlayer)) {
            currentPlayer = (currentPlayer + 1) % players.length;
        }
    }
    
    private void moveToNextStreet() {
        // Reset betting for the next street
        bettingManager.initializeNewHand();
        roundStartPlayer = -1;  // Reset for next street
        
        switch (stateManager.getGameState()) {
            case START:
                cardManager.dealNextStreet(GameState.FLOP);
                stateManager.setGameState(GameState.FLOP);
                break;
            case FLOP:
                cardManager.dealNextStreet(GameState.TURN);
                stateManager.setGameState(GameState.TURN);
                break;
            case TURN:
                cardManager.dealNextStreet(GameState.RIVER);
                stateManager.setGameState(GameState.RIVER);
                break;
            case RIVER:
                stateManager.setGameState(GameState.FINISH);
                determineWinner();
                break;
            default:
                break;
        }
    }

    private boolean isRoundComplete() {
        // First, check if we've gone around the table at least once
        if (roundStartPlayer == -1) {
            roundStartPlayer = currentPlayer;
            return false;
        }
        
        // Get all active players and their bets
        List<Integer> activePlayers = stateManager.getActivePlayers();
        if (activePlayers.isEmpty()) {
            return false;
        }
        
        // Check if all active players have equal bets
        int targetBet = bettingManager.getCurrentBet();
        for (int playerIndex : activePlayers) {
            Player player = players[playerIndex];
            if (player.getCommitted() != targetBet) {
                return false;
            }
        }
        
        // If we've gone around the table and all bets are equal, the round is complete
        return currentPlayer == roundStartPlayer;
    }

    private void moveToNextPlayer() {
        do {
            currentPlayer = (currentPlayer + 1) % players.length;
        } while (!stateManager.isPlayerActive(currentPlayer));
    }

    private void resetToFirstPlayer() {
        // In pre-flop, first player is after big blind (dealer + 3)
        // In all other streets, first player is small blind (dealer + 1)
        int offset = stateManager.getGameState() == GameState.START ? 3 : 1;
        currentPlayer = (stateManager.getDealerIndex() + offset) % players.length;
        while (!stateManager.isPlayerActive(currentPlayer)) {
            currentPlayer = (currentPlayer + 1) % players.length;
        }
        roundStartPlayer = -1;  // Reset for new betting round
    }

    public boolean processNextAction() {
        if (stateManager.getGameState() == GameState.FINISH) {
            return false;
        }
        
        // Initialize roundStartPlayer if not set
        if (roundStartPlayer == -1) {
            roundStartPlayer = currentPlayer;
        }
        
        // Get and process player's action
        Action playerAction = players[currentPlayer].getAction(
            bettingManager.getCurrentBet(),
            cardManager.getCommunityCards(),
            bettingManager.getPot()
        );
        boolean actionTaken = bettingManager.processAction(playerAction, currentPlayer);
        
        if (!actionTaken) {
            return false;
        }

        // Record the action
        addHandAnalysis(getActionDescription(players[currentPlayer], playerAction));
        
        // Check if only one player remains
        if (stateManager.getActivePlayerCount() == 1) {
            List<Integer> activePlayers = stateManager.getActivePlayers();
            currentPlayer = activePlayers.get(0);
            stateManager.setGameState(GameState.FINISH);
            determineWinner();
            return true;
        }
        
        // Move to next player
        moveToNextPlayer();
        
        // Check if round is complete
        if (isRoundComplete()) {
            moveToNextStreet();
            
            // Reset player order for next street if game isn't finished
            if (stateManager.getGameState() != GameState.FINISH) {
                resetToFirstPlayer();
                roundStartPlayer = currentPlayer;  // Set the new round start player
            }
            return true;
        }
        
        return false;
    }
    
    private String getActionDescription(Player player, Action action) {
        // Get player's current hand strength
        ArrayList<Card> playerCards = new ArrayList<>(player.getCards());
        playerCards.addAll(cardManager.getCommunityCards());
        HandEvaluator handEval = new HandEvaluator(playerCards);
        String handDesc = handEval.getString();
        
        String actionDesc;
        switch (action) {
            case FOLD:
                actionDesc = player.getName() + " folds";
                break;
            case CHECK:
                actionDesc = player.getName() + " checks";
                break;
            case CALL:
                actionDesc = player.getName() + " calls $" + bettingManager.getCurrentBet();
                break;
            case RAISE:
                actionDesc = player.getName() + " raises to $" + action.getAmount();
                break;
            default:
                return "";
        }
        
        // Only add hand description if player hasn't folded and there are community cards
        if (!player.isFolded() && !cardManager.getCommunityCards().isEmpty()) {
            actionDesc += " (" + handDesc + ")";
        }
        
        return actionDesc;
    }
    
    private void determineWinner() {
        // Find active players
        List<Integer> activeIndices = new ArrayList<>();
        for (int i = 0; i < players.length; i++) {
            if (stateManager.isPlayerActive(i)) {
                activeIndices.add(i);
            }
        }
        
        if (activeIndices.size() == 1) {
            // Only one player left - they win
            int winner = activeIndices.get(0);
            bettingManager.awardPot(winner);
            addHandAnalysis(players[winner].getName() + " wins $" + bettingManager.getPot());
        } else {
            // Compare hands
            int bestRank = -1;
            List<Integer> winners = new ArrayList<>();
            
            for (int index : activeIndices) {
                ArrayList<Card> playerCards = new ArrayList<>(players[index].getCards());
                playerCards.addAll(cardManager.getCommunityCards());
                HandEvaluator evaluator = new HandEvaluator(playerCards);
                int rank = evaluator.getRanking();
                
                if (rank > bestRank) {
                    bestRank = rank;
                    winners.clear();
                    winners.add(index);
                } else if (rank == bestRank) {
                    winners.add(index);
                }
            }
            
            if (winners.size() == 1) {
                int winner = winners.get(0);
                bettingManager.awardPot(winner);
                ArrayList<Card> winningCards = new ArrayList<>(players[winner].getCards());
                winningCards.addAll(cardManager.getCommunityCards());
                HandEvaluator winnerHand = new HandEvaluator(winningCards);
                addHandAnalysis(players[winner].getName() + " wins $" + bettingManager.getPot() + 
                              " with " + winnerHand.getString());
            } else {
                int[] winnerArray = new int[winners.size()];
                for (int i = 0; i < winners.size(); i++) {
                    winnerArray[i] = winners.get(i);
                }
                bettingManager.splitPot(winnerArray);
                
                StringBuilder sb = new StringBuilder("Split pot between: ");
                for (int i = 0; i < winners.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(players[winners.get(i)].getName());
                }
                ArrayList<Card> winningCards = new ArrayList<>(players[winners.get(0)].getCards());
                winningCards.addAll(cardManager.getCommunityCards());
                HandEvaluator winnerHand = new HandEvaluator(winningCards);
                sb.append(" with ").append(winnerHand.getString());
                addHandAnalysis(sb.toString());
            }
        }
    }
    
    private void addHandAnalysis(String analysis) {
        handAnalysis.add(analysis);
    }
    
    // Getters
    public List<String> getHandAnalysis() { return handAnalysis; }
    public int getSmallBlind() { return smallBlind; }
    public int getBigBlind() { return bigBlind; }
    public int getCurrentPlayer() { return currentPlayer; }
    public GameState getGameState() { return stateManager.getGameState(); }
    public int getPot() { return bettingManager.getPot(); }
    public int getDealerIndex() { return stateManager.getDealerIndex(); }
    public CardManager getCardManager() { return cardManager; }
    public boolean isTestMode() { return testMode; }
} 