package com.mackuntu.poker.game;

import java.util.ArrayList;
import java.util.List;
import com.mackuntu.poker.Player.Player;

public class PokerPlayerStateManager implements PlayerStateManager {
    private final Player[] players;
    private final ArrayList<Integer> activePlayers;
    private int currentPlayerIndex;
    
    public PokerPlayerStateManager(Player[] players) {
        this.players = players;
        this.activePlayers = new ArrayList<>();
        initializeActivePlayers();
    }
    
    @Override
    public void initializeActivePlayers() {
        activePlayers.clear();
        for (int i = 0; i < players.length; i++) {
            if (players[i].getMoney() > 0) {
                activePlayers.add(i);
            }
        }
        if (!activePlayers.isEmpty()) {
            currentPlayerIndex = activePlayers.get(0);
        }
    }
    
    @Override
    public void reinitializePlayers() {
        for (Player player : players) {
            player.reInit();
        }
        initializeActivePlayers();
    }
    
    @Override
    public boolean isPlayerActive(int playerIndex) {
        return !players[playerIndex].isFolded() && players[playerIndex].getMoney() > 0;
    }
    
    @Override
    public void handlePlayerFold(int playerIndex) {
        players[playerIndex].fold();
        activePlayers.remove(Integer.valueOf(playerIndex));
    }
    
    @Override
    public void handlePlayerAllIn(int playerIndex) {
        activePlayers.remove(Integer.valueOf(playerIndex));
        if (currentPlayerIndex == playerIndex && !activePlayers.isEmpty()) {
            moveToNextPlayer();
        }
    }
    
    @Override
    public List<Integer> getActivePlayers() {
        return new ArrayList<>(activePlayers);
    }
    
    @Override
    public int getActivePlayerCount() {
        return activePlayers.size();
    }
    
    @Override
    public int getPlayerCount() {
        return players.length;
    }
    
    @Override
    public Player getPlayer(int playerIndex) {
        return players[playerIndex];
    }
    
    @Override
    public void moveToNextPlayer() {
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
    
    @Override
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
} 