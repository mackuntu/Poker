package com.mackuntu.poker.game;

import java.util.ArrayList;
import java.util.List;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Player.PlayerState;

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
        // All players with money start as active in the new hand
        for (int i = 0; i < players.length; i++) {
            if (players[i].getMoney() > 0) {
                players[i].setState(PlayerState.ACTIVE);
                activePlayers.add(i);
            }
        }
        if (!activePlayers.isEmpty()) {
            currentPlayerIndex = activePlayers.get(0);
        }
    }
    
    @Override
    public void reinitializePlayers() {
        // Reset all player states for new hand
        for (Player player : players) {
            player.reInit();  // This clears cards and resets state based on money
        }
        // Initialize active players list with all players who have money
        initializeActivePlayers();
    }
    
    @Override
    public boolean isPlayerActive(int playerIndex) {
        // During a hand, a player is active if they haven't folded and aren't all-in
        return players[playerIndex].isActive();
    }
    
    @Override
    public void handlePlayerFold(int playerIndex) {
        players[playerIndex].fold();  // This sets state to FOLDED
        activePlayers.remove(Integer.valueOf(playerIndex));
        
        // If the folding player was the current player, move to the next one
        if (currentPlayerIndex == playerIndex && !activePlayers.isEmpty()) {
            // Find the next active player in sequence
            int nextIndex = 0;
            for (int i = 0; i < activePlayers.size(); i++) {
                if (activePlayers.get(i) > playerIndex) {
                    nextIndex = i;
                    break;
                }
            }
            currentPlayerIndex = activePlayers.get(nextIndex);
        }
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
        // Refresh active players list based on current state
        ArrayList<Integer> currentActive = new ArrayList<>();
        for (int i = 0; i < players.length; i++) {
            if (isPlayerActive(i)) {
                currentActive.add(i);
            }
        }
        // Update our internal list
        activePlayers.clear();
        activePlayers.addAll(currentActive);
        return new ArrayList<>(activePlayers);
    }
    
    @Override
    public int getActivePlayerCount() {
        return getActivePlayers().size();  // Use refreshed list
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