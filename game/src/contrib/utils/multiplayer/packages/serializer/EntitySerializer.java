package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Component;
import core.Entity;

public class EntitySerializer extends Serializer<Entity> {
    @Override
    public void write(Kryo kryo, Output output, Entity object) {
        output.writeString(object.name());
        output.writeInt(object.localeID());
        output.writeInt(object.globalID());
        long size = object.componentStream().count();
        output.writeLong(size);
        object.componentStream().forEach((component) -> {
            kryo.writeClass(output, component.getClass());
            kryo.writeObject(output, component);
        });
    }

    @Override
    public Entity read(Kryo kryo, Input input, Class<Entity> type) {
        String name = input.readString();
        int localeID = input.readInt();
        int globalID = input.readInt();
        long size = input.readLong();
        //Todo - change between load map request and response so entities get created in the right way
        Entity e = new Entity(name, localeID, globalID);

        for (int i = 0; i < size; i++){
            Class <? extends Component> klass = kryo.readClass(input).getType();
            switch (klass.getSimpleName()){
                case "DrawComponent":
                    kryo.readObject(input,klass,new DrawComponentSerializer(e));
                    break;
                case "PositionComponent":
                    kryo.readObject(input,klass,new PositionComponentSerializer(e));
                    break;
                case "VelocityComponent":
                    kryo.readObject(input,klass,new VelocityComponentSerializer(e));
                    break;
                case "CollideComponent":
                    kryo.readObject(input,klass,new CollideComponentSerializer(e));
                    break;
                case "HealthComponent":
                    kryo.readObject(input,klass,new HealthComponentSerializer(e));
                    break;
                case "InteractionComponent":
                    kryo.readObject(input,klass,new InteractionComponentSerializer(e));
                    break;
                case "InventoryComponent":
                    kryo.readObject(input,klass,new InventoryComponentSerializer(e));
                    break;
                case "ItemComponent":
                    kryo.readObject(input,klass,new ItemComponentSerializer(e));
                    break;
                case "MultiplayerComponent":
                    kryo.readObject(input,klass,new MultiplayerComponentSerializer(e));
                    break;
                case "ProjectileComponent":
                    kryo.readObject(input,klass,new ProjectileComponentSerializer(e));
                    break;
                case "StatsComponent":
                    kryo.readObject(input,klass,new StatsComponentSerializer(e));
                    break;
                case "XPComponent":
                    kryo.readObject(input,klass,new XPComponentSerializer(e));
                    break;
                default:
                    break;
            }
        }
        return e;
    }
}
