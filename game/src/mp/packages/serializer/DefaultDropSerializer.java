package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.item.DefaultDrop;

public class DefaultDropSerializer extends Serializer<DefaultDrop> {
    @Override
    public void write(Kryo kryo, Output output, DefaultDrop object) {
        kryo.writeClass(output, object.getClass());
    }

    @Override
    public DefaultDrop read(Kryo kryo, Input input, Class<DefaultDrop> type) {
        return new DefaultDrop();
    }
}
