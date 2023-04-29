package mp.client;

import level.elements.ILevel;
import tools.Point;

import java.util.HashMap;

public interface IMultiplayerClientObserver {
    void onInitializeServerResponseReceived(boolean isSucceed, int clientId);
    void onJoinSessionResponseReceived(ILevel level, int clientId, HashMap<Integer, Point> heroPositionByClientId);
    void onHeroPositionsChangedEventReceived(HashMap<Integer, Point> heroPositionByClientId);
    void onConnected();
    void onDisconnected();
}
