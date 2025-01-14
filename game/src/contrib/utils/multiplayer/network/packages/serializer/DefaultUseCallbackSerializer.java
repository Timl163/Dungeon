package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.item.DefaultUseCallback;

/** Custom serializer to send and retrieve objects of {@link DefaultUseCallback}. */
public class DefaultUseCallbackSerializer extends Serializer<DefaultUseCallback> {
    @Override
    public void write(Kryo kryo, Output output, DefaultUseCallback object) {
        kryo.writeClass(output, object.getClass());
    }

    @Override
    public DefaultUseCallback read(Kryo kryo, Input input, Class<DefaultUseCallback> type) {
        return new DefaultUseCallback();
    }
}
