package contrib.utils.multiplayer.client;

import core.level.elements.ILevel;
import core.utils.Point;
import contrib.utils.multiplayer.packages.GameState;

import java.net.InetAddress;
import java.util.HashMap;

public interface IMultiplayerClientObserver {
    void onInitServerResponseReceived(boolean isSucceed, int clientId);
    void onLoadMapResponseReceived(boolean isSucceed, ILevel level, HashMap<Integer, Point> heroPositionByClientId);
    void onChangeMapRequest();
    void onJoinSessionResponseReceived(boolean isSucceed, ILevel level, int clientId, HashMap<Integer, Point> heroPositionByClientId);
    void onGameStateUpdateEventReceived(GameState gameState);
    void onUpdateOwnPositionResponseReceived();
    void onConnected(InetAddress address);
    void onDisconnected(InetAddress address);
}