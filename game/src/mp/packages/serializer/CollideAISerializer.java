package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.fight.CollideAI;

public class CollideAISerializer extends Serializer<CollideAI> {
    @Override
    public void write(Kryo kryo, Output output, CollideAI object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.getRushRange());
    }

    @Override
    public CollideAI read(Kryo kryo, Input input, Class<CollideAI> type) {
        return new CollideAI(input.readFloat());
    }
}
