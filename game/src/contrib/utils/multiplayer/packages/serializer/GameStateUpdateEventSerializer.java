package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.event.GameStateUpdateEvent;
import core.Entity;
import core.utils.Point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameStateUpdateEventSerializer extends Serializer<GameStateUpdateEvent> {

    @Override
    public void write(Kryo kryo, Output output, GameStateUpdateEvent object) {
        kryo.writeObject(output, object.entities());
    }

    @Override
    public GameStateUpdateEvent read(Kryo kryo, Input input, Class<GameStateUpdateEvent> type) {
        final Set<Entity> entities = kryo.readObject(input, HashSet.class);
        return new GameStateUpdateEvent(entities);
    }
}
