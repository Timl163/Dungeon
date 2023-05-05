package mp.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import mp.packages.NetworkSetup;
import mp.packages.response.InitializeServerResponse;
import mp.packages.response.JoinSessionResponse;
import mp.packages.response.PingResponse;
import mp.packages.event.HeroPositionsChangedEvent;
import mp.packages.response.UpdateOwnPositionResponse;
import tools.Point;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MultiplayerClient extends Listener {

    // TODO: Outsource config parameters
    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final Integer maxObjectSizeExpected = 8000000;
    private static final Integer writeBufferSize = maxObjectSizeExpected;
    private static final Integer objectBufferSize = maxObjectSizeExpected;
    private static final Integer connectionTimeout = 5000;
    private static final Client client = new Client(writeBufferSize, objectBufferSize);
    private final ArrayList<IMultiplayerClientObserver> observers = new ArrayList<>();

    public MultiplayerClient() {
        client.addListener(this);
        NetworkSetup.register(client);
        client.start();
    }

    @Override
    public void connected(Connection connection) {
//        System.out.println("Connected to server!");
    }

    @Override
    public void disconnected(Connection connection) {
        for (IMultiplayerClientObserver observer: observers) {
            observer.onDisconnected();
        }
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingResponse pingResponse) {
            System.out.println("Ping response received. Time: " + pingResponse.getTime());
        } else if (object instanceof InitializeServerResponse initializeServerResponse){
            final boolean isSucceed = initializeServerResponse.getIsSucceed();
            final Point initialHeroPosition = initializeServerResponse.getInitialHeroPosition();
            for (IMultiplayerClientObserver observer: observers) {
                observer.onInitializeServerResponseReceived(isSucceed, connection.getID(), initialHeroPosition);
            }
        } else if (object instanceof JoinSessionResponse response) {
            for (IMultiplayerClientObserver observer: observers) {
                observer.onJoinSessionResponseReceived(
                    response.getIsSucceed(),
                    response.getLevel(),
                    response.getClientId(),
                    response.getHeroPositionByClientId()
                );
            }
        } else if (object instanceof HeroPositionsChangedEvent){
            HashMap<Integer, Point> heroPositionByClientId = ((HeroPositionsChangedEvent)object).getHeroPositionByClientId();
            for (IMultiplayerClientObserver observer: observers){
                observer.onHeroPositionsChangedEventReceived(heroPositionByClientId);
            }
        } else if (object instanceof UpdateOwnPositionResponse) {
            for (IMultiplayerClientObserver observer: observers){
                observer.onUpdateOwnPositionResponseReceived();
            }
        }
    }

    public void send(Object object) {
        client.sendTCP(object);
    }

    public boolean connectToHost(String address, int port) {
        try {
            client.connect(connectionTimeout, address, port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addObserver(IMultiplayerClientObserver observer) {
        observers.add(observer);
    }
}
