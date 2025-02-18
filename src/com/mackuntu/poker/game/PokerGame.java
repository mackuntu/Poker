package com.mackuntu.poker.game;

import java.util.ArrayList;
import java.util.List;
import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Evaluator.HandEvaluator;

public class PokerGame {
    private final Player[] players;
    private final PlayerStateManager playerManager;
    private final DealerPositionManager dealerManager;
    private final GameMessageManager messageManager;
    private final BettingManager bettingManager;
    private final CardManager cardManager;
    private final ArrayList<String> handAnalysis;
    private final boolean testMode;
    private GameState state;
    private int currentPlayer;
    private int smallBlind;
    private int bigBlind;
    private int handsPlayed;
    private int roundStartPlayer = -1;
    
    public PokerGame(Player[] players, boolean testMode) {
        this.players = players;
        this.testMode = testMode;
        this.playerManager = new PokerPlayerStateManager(players);
        this.dealerManager = new StandardDealerManager(players.length);
        this.messageManager = new StandardGameMessageManager();
        this.state = GameState.START;
        this.smallBlind = 20;
        this.bigBlind = 40;
        this.bettingManager = new BettingManager(players);
        this.cardManager = new CardManager(players, testMode);
        this.handAnalysis = new ArrayList<>();
        this.handsPlayed = 0;
        
        // Initialize player positions
        for (int i = 0; i < players.length; i++) {
            players[i].setMoney(1000);  // Starting stack
        }
    }
    
    public boolean hasEnoughPlayersWithMoney() {
        int count = 0;
        for (Player player : players) {
            if (player.getMoney() > 0) {
                count++;
            }
        }
        return count >= 2;
    }

    public void startNewHand() {
        // First check if we have enough players with money
        if (!hasEnoughPlayersWithMoney()) {
            state = GameState.FINISH;
            addHandAnalysis("Game over - not enough players with money to continue");
            return;
        }

        handsPlayed++;
        if (handsPlayed % 10 == 0) {
            smallBlind *= 2;
            bigBlind *= 2;
            addHandAnalysis("Blinds increased to " + smallBlind + "/" + bigBlind);
        }
        
        // Reset game state
        state = GameState.START;
        playerManager.reinitializePlayers();  // This will make all players with money active for the new hand
        bettingManager.initializeNewHand();
        cardManager.initializeNewHand();
        handAnalysis.clear();
        
        // Find next valid small blind position (player must have money)
        int smallBlindPos = (dealerManager.getDealerPosition() + 1) % players.length;
        while (players[smallBlindPos].getMoney() <= 0) {
            smallBlindPos = (smallBlindPos + 1) % players.length;
        }
        
        // Find next valid big blind position (player must have money)
        int bigBlindPos = (smallBlindPos + 1) % players.length;
        while (players[bigBlindPos].getMoney() <= 0) {
            bigBlindPos = (bigBlindPos + 1) % players.length;
        }
        
        // Post blinds if players can afford them
        Player smallBlindPlayer = players[smallBlindPos];
        Player bigBlindPlayer = players[bigBlindPos];
        
        bettingManager.postBlind(smallBlindPos, Math.min(smallBlind, smallBlindPlayer.getMoney()));
        bettingManager.postBlind(bigBlindPos, Math.min(bigBlind, bigBlindPlayer.getMoney()));
        
        if (smallBlindPlayer.getMoney() <= 0) {
            handlePlayerAllIn(smallBlindPos);
        }
        if (bigBlindPlayer.getMoney() <= 0) {
            handlePlayerAllIn(bigBlindPos);
        }
        
        // Deal cards
        cardManager.dealInitialCards(dealerManager.getDealerPosition());
        
        // Set initial player to first player after big blind with money
        currentPlayer = (bigBlindPos + 1) % players.length;
        while (players[currentPlayer].getMoney() <= 0) {
            currentPlayer = (currentPlayer + 1) % players.length;
        }
    }
    
    private void handlePlayerAllIn(int playerIndex) {
        if (players[playerIndex].getMoney() <= 0) {
            playerManager.handlePlayerAllIn(playerIndex);
            messageManager.addMessage(players[playerIndex].getName() + " has been eliminated (out of money)");
        }
    }
    
    private void moveToNextStreet() {
        // Reset betting for the next street
        bettingManager.initializeNewStreet();
        roundStartPlayer = -1;  // Reset for next street
        
        switch (state) {
            case START:
                cardManager.dealNextStreet(GameState.FLOP);
                state = GameState.FLOP;
                break;
            case FLOP:
                cardManager.dealNextStreet(GameState.TURN);
                state = GameState.TURN;
                break;
            case TURN:
                cardManager.dealNextStreet(GameState.RIVER);
                state = GameState.RIVER;
                break;
            case RIVER:
                state = GameState.FINISH;
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
        List<Integer> activePlayers = getActivePlayers();
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
        } while (!isPlayerActive(currentPlayer));
    }

    private void resetToFirstPlayer() {
        // In pre-flop, first player is after big blind (dealer + 3)
        // In all other streets, first player is small blind (dealer + 1)
        int offset = state == GameState.START ? 3 : 1;
        currentPlayer = (dealerManager.getDealerPosition() + offset) % players.length;
        while (!isPlayerActive(currentPlayer)) {
            currentPlayer = (currentPlayer + 1) % players.length;
        }
        roundStartPlayer = -1;  // Reset for new betting round
    }

    public boolean processNextAction() {
        if (state == GameState.FINISH) {
            System.out.println("In FINISH state - checking if game can continue");
            
            // Check if game is truly over (not enough players with money)
            if (!hasEnoughPlayersWithMoney()) {
                System.out.println("Game is over - not enough players with money");
                return false;  // Signal that no more actions can be taken
            }
            
            System.out.println("Starting new hand");
            dealerManager.advanceDealer(playerManager);
            startNewHand();
            return true;
        }
        
        // Initialize roundStartPlayer if not set
        if (roundStartPlayer == -1) {
            roundStartPlayer = currentPlayer;
            System.out.println("Setting round start player to: " + currentPlayer);
        }
        
        // Get and process player's action
        System.out.println("Getting action for player " + currentPlayer + " (" + players[currentPlayer].getName() + ")");
        Action playerAction = players[currentPlayer].getAction(
            bettingManager.getCurrentBet(),
            cardManager.getCommunityCards(),
            bettingManager.getPot()
        );
        System.out.println("Player action: " + playerAction);
        
        boolean actionTaken = bettingManager.processAction(playerAction, currentPlayer);
        System.out.println("Action taken: " + actionTaken);
        
        if (!actionTaken) {
            System.out.println("Invalid action: " + playerAction + ". Forcing fold.");
            // Force a fold if the player makes an invalid action
            playerAction = Action.FOLD;
            actionTaken = bettingManager.processAction(playerAction, currentPlayer);
            if (!actionTaken) {
                System.out.println("Critical error: Could not process fold action");
                return false;
            }
        }

        // Record the action
        addHandAnalysis(getActionDescription(players[currentPlayer], playerAction));
        
        // Check if only one player remains
        if (getActivePlayerCount() == 1) {
            System.out.println("Only one player remains active!");
            List<Integer> activePlayers = getActivePlayers();
            currentPlayer = activePlayers.get(0);
            System.out.println("Last player standing: " + players[currentPlayer].getName());
            determineWinner();  // Award pot to the last remaining player
            state = GameState.FINISH;  // Set state to FINISH after awarding pot
            dealerManager.advanceDealer(playerManager);  // Advance dealer for next hand
            return true;
        }
        
        // Move to next player
        int oldPlayer = currentPlayer;
        moveToNextPlayer();
        System.out.println("Moved from player " + oldPlayer + " to " + currentPlayer);
        
        // Check if round is complete
        if (isRoundComplete()) {
            System.out.println("Round is complete - moving to next street");
            moveToNextStreet();
            
            // Reset player order for next street if game isn't finished
            if (state != GameState.FINISH) {
                resetToFirstPlayer();
                roundStartPlayer = currentPlayer;  // Set the new round start player
                System.out.println("Reset to first player: " + currentPlayer);
            } else {
                System.out.println("Game finished - dealer will advance");
                dealerManager.advanceDealer(playerManager);
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
            if (isPlayerActive(i)) {
                activeIndices.add(i);
            }
        }
        
        if (activeIndices.size() == 1) {
            // Only one player left - they win
            int winner = activeIndices.get(0);
            int potAmount = bettingManager.getPot();
            bettingManager.awardPot(winner);
            String message = players[winner].getName() + " wins $" + potAmount;
            addHandAnalysis(message);
            messageManager.addMessage(message);
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
            
            int potAmount = bettingManager.getPot();
            if (winners.size() == 1) {
                int winner = winners.get(0);
                bettingManager.awardPot(winner);
                ArrayList<Card> winningCards = new ArrayList<>(players[winner].getCards());
                winningCards.addAll(cardManager.getCommunityCards());
                HandEvaluator winnerHand = new HandEvaluator(winningCards);
                String message = players[winner].getName() + " wins $" + potAmount + 
                               " with " + winnerHand.getString();
                addHandAnalysis(message);
                messageManager.addMessage(message);
            } else {
                int[] winnerArray = new int[winners.size()];
                for (int i = 0; i < winners.size(); i++) {
                    winnerArray[i] = winners.get(i);
                }
                bettingManager.splitPot(winnerArray);
                
                StringBuilder sb = new StringBuilder("Split pot ($" + potAmount + ") between: ");
                for (int i = 0; i < winners.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(players[winners.get(i)].getName());
                }
                ArrayList<Card> winningCards = new ArrayList<>(players[winners.get(0)].getCards());
                winningCards.addAll(cardManager.getCommunityCards());
                HandEvaluator winnerHand = new HandEvaluator(winningCards);
                sb.append(" with ").append(winnerHand.getString());
                String message = sb.toString();
                addHandAnalysis(message);
                messageManager.addMessage(message);
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
    public GameState getGameState() { return state; }
    public int getPot() { return bettingManager.getPot(); }
    public int getDealerIndex() { return dealerManager.getDealerPosition(); }
    public CardManager getCardManager() { return cardManager; }
    public boolean isTestMode() { return testMode; }
    public List<Integer> getActivePlayers() { return playerManager.getActivePlayers(); }
    public boolean isPlayerActive(int playerIndex) { return playerManager.isPlayerActive(playerIndex); }
    public int getActivePlayerCount() { return playerManager.getActivePlayerCount(); }
} 