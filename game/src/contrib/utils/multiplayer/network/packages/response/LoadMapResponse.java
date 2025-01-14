package contrib.utils.multiplayer.network.packages.response;

import static java.util.Objects.requireNonNull;

import contrib.utils.multiplayer.network.packages.GameState;
import contrib.utils.multiplayer.network.packages.request.LoadMapRequest;

/** Response of {@link LoadMapRequest} */
public class LoadMapResponse {

    private final boolean isSucceed;
    private final GameState gameState;

    /**
     * Create new instance.
     *
     * @param isSucceed State whether game state was set up or not.
     * @param gameState Game
     */
    public LoadMapResponse(final boolean isSucceed, final GameState gameState) {
        this.isSucceed = isSucceed;
        this.gameState = requireNonNull(gameState);
    }

    /**
     * @return State whether loading map successes or not.
     */
    public boolean isSucceed() {
        return isSucceed;
    }

    /**
     * @return Game state.
     */
    public GameState gameState() {
        return gameState;
    }
}
