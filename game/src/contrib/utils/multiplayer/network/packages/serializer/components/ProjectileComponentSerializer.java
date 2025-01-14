package contrib.utils.multiplayer.network.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.ProjectileComponent;

import core.Entity;
import core.utils.Point;

/** Custom serializer to send and retrieve objects of {@link ProjectileComponent}. */
public class ProjectileComponentSerializer extends Serializer<ProjectileComponent> {
    private Entity entity;

    /** Create new serializer for {@link ProjectileComponent}. */
    public ProjectileComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link ProjectileComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public ProjectileComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, ProjectileComponent object) {
        kryo.writeObject(output, object.goalLocation());
        kryo.writeObject(output, object.startPosition());
    }

    @Override
    public ProjectileComponent read(Kryo kryo, Input input, Class<ProjectileComponent> type) {
        Point goalPosition = kryo.readObject(input, Point.class);
        Point startPosition = kryo.readObject(input, Point.class);
        return new ProjectileComponent(entity, startPosition, goalPosition);
    }
}
