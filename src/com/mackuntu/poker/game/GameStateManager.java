package com.mackuntu.poker.game;

import java.util.ArrayList;
import java.util.List;
import com.mackuntu.poker.Player.Player;

public class GameStateManager {
    private Player[] players;
    private ArrayList<Integer> activePlayers;
    private int currentPlayerIndex;
    private GameState state;
    private int dealerIndex;
    
    public GameStateManager(Player[] players) {
        this.players = players;
        this.activePlayers = new ArrayList<>();
        this.state = GameState.START;
        this.dealerIndex = 0;
        initializeActivePlayers();
    }
    
    private void initializeActivePlayers() {
        activePlayers.clear();
        for (int i = 0; i < players.length; i++) {
            if (players[i].canAct()) {
                activePlayers.add(i);
            }
        }
        if (!activePlayers.isEmpty()) {
            currentPlayerIndex = activePlayers.get(0);
        }
    }
    
    public void initializeNewHand() {
        for (Player player : players) {
            player.reInit();
        }
        initializeActivePlayers();
        state = GameState.START;
    }
    
    public void handlePlayerAction(int playerIndex, String action) {
        if (action.equals("FOLD")) {
            players[playerIndex].fold();
            activePlayers.remove(Integer.valueOf(playerIndex));
        }
        moveToNextPlayer();
    }
    
    public void handleMoneyChange(int playerIndex) {
        if (!players[playerIndex].canAct()) {
            activePlayers.remove(Integer.valueOf(playerIndex));
            if (currentPlayerIndex == playerIndex && !activePlayers.isEmpty()) {
                moveToNextPlayer();
            }
        }
    }
    
    private void moveToNextPlayer() {
        if (activePlayers.isEmpty()) {
            return;
        }
        
        int currentActiveIndex = activePlayers.indexOf(currentPlayerIndex);
        if (currentActiveIndex == -1) {
            currentPlayerIndex = activePlayers.get(0);
            return;
        }
        
        currentActiveIndex = (currentActiveIndex + 1) % activePlayers.size();
        currentPlayerIndex = activePlayers.get(currentActiveIndex);
    }
    
    public void setGameState(GameState state) {
        this.state = state;
    }
    
    public GameState getGameState() {
        return state;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public boolean isPlayerActive(int index) {
        return activePlayers.contains(index);
    }
    
    public int getActivePlayerCount() {
        return activePlayers.size();
    }
    
    public List<Integer> getActivePlayers() {
        return new ArrayList<>(activePlayers);
    }
    
    public int getDealerIndex() {
        return dealerIndex;
    }
    
    public void setDealerIndex(int index) {
        this.dealerIndex = index;
    }
    
    public void advanceDealer() {
        do {
            dealerIndex = (dealerIndex + 1) % players.length;
        } while (!players[dealerIndex].canAct());
    }
} 