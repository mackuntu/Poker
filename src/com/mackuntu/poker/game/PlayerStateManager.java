package com.mackuntu.poker.game;

import java.util.List;
import com.mackuntu.poker.Player.Player;

public interface PlayerStateManager {
    boolean isPlayerActive(int playerIndex);
    void handlePlayerFold(int playerIndex);
    void handlePlayerAllIn(int playerIndex);
    List<Integer> getActivePlayers();
    int getActivePlayerCount();
    int getPlayerCount();
    void initializeActivePlayers();
    void reinitializePlayers();
    Player getPlayer(int playerIndex);
    void moveToNextPlayer();
    int getCurrentPlayerIndex();
} 