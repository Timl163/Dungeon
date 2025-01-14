package contrib.utils.multiplayer.network.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;

/** Custom serializer to send and retrieve objects of {@link PositionComponent}. */
public class PositionComponentSerializer extends Serializer<PositionComponent> {
    private Entity entity;

    /** Create new serializer for {@link PositionComponent}. */
    public PositionComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link PositionComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public PositionComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, PositionComponent object) {
        kryo.writeObject(output, object.position());
    }

    @Override
    public PositionComponent read(Kryo kryo, Input input, Class<PositionComponent> type) {
        Point position = kryo.readObject(input, Point.class);
        return new PositionComponent(entity, position);
    }
}
