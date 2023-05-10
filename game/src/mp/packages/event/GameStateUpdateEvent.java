package mp.packages.event;

import mp.packages.GameState;

import static java.util.Objects.requireNonNull;

public class GameStateUpdateEvent {

    private final GameState gameState;

    public GameStateUpdateEvent(final GameState gameState){
        requireNonNull(gameState);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
