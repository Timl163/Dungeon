package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.ai.fight.CollideAI;

/** Custom serializer to send and retrieve objects of {@link CollideAI}. */
public class CollideAISerializer extends Serializer<CollideAI> {
    @Override
    public void write(Kryo kryo, Output output, CollideAI object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.rushRange());
    }

    @Override
    public CollideAI read(Kryo kryo, Input input, Class<CollideAI> type) {
        return new CollideAI(input.readFloat());
    }
}
