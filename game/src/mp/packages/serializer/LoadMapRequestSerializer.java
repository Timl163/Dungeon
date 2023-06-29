package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;
import mp.packages.request.LoadMapRequest;

import java.util.HashSet;
import java.util.Set;

public class LoadMapRequestSerializer extends Serializer<LoadMapRequest> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapRequest object) {
        kryo.writeObject(output, object.getLevel());
        Set<Entity> currentEntities = object.getCurrentEntities();
        output.writeInt(currentEntities.size());
        for(Entity entity : currentEntities){
            kryo.writeObject(output, entity);
        }
    }

    @Override
    public LoadMapRequest read(Kryo kryo, Input input, Class<LoadMapRequest> type) {
        final ILevel level = kryo.readObject(input, ILevel.class);
        int size = input.readInt();
        Set<Entity> currentEntities = new HashSet<>();
        for (int i = 0; i < size; i++){
            currentEntities.add(kryo.readObject(input, Entity.class));
        }
        return new LoadMapRequest(level, currentEntities);
    }
}
