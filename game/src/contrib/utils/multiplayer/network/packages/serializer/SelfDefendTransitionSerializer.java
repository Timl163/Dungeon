package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.ai.transition.SelfDefendTransition;

/** Custom serializer to send and retrieve objects of {@link SelfDefendTransition}. */
public class SelfDefendTransitionSerializer extends Serializer<SelfDefendTransition> {
    @Override
    public void write(Kryo kryo, Output output, SelfDefendTransition object) {
        kryo.writeClass(output, object.getClass());
    }

    @Override
    public SelfDefendTransition read(Kryo kryo, Input input, Class<SelfDefendTransition> type) {
        return new SelfDefendTransition();
    }
}
