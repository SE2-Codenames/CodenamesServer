package model;

public enum GameState {
    LOBBY,          // Players joining/selecting teams
    SPYMASTER_TURN, // Spymaster giving clue
    OPERATIVE_TURN, // Operatives guessing
    GAME_OVER       // Game ended
}
