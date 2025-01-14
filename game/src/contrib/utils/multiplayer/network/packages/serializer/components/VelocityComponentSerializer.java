package contrib.utils.multiplayer.network.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import core.Entity;
import core.components.VelocityComponent;

/** Custom serializer to send and retrieve objects of {@link VelocityComponent}. */
public class VelocityComponentSerializer extends Serializer<VelocityComponent> {
    private Entity entity;

    /**
     * Create new serializer for {@link VelocityComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public VelocityComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link VelocityComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public VelocityComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, VelocityComponent object) {
        output.writeFloat(object.xVelocity());
        output.writeFloat(object.yVelocity());
        output.writeFloat(object.currentXVelocity());
        output.writeFloat(object.currentYVelocity());
    }

    @Override
    public VelocityComponent read(Kryo kryo, Input input, Class<VelocityComponent> type) {
        final float xVelocity = input.readFloat();
        final float YVelocity = input.readFloat();
        final float currentXVelocity = input.readFloat();
        final float currentYVelocity = input.readFloat();
        final VelocityComponent velocityComponent =
                new VelocityComponent(entity, xVelocity, YVelocity);
        velocityComponent.currentXVelocity(currentXVelocity);
        velocityComponent.currentYVelocity(currentYVelocity);
        return velocityComponent;
    }
}
