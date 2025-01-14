package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.function.Consumer;

/** Custom serializer to send and retrieve objects of {@link Consumer}. */
public class ConsumerSerializer extends Serializer<Consumer> {
    @Override
    public void write(Kryo kryo, Output output, Consumer object) {}

    @Override
    public Consumer read(Kryo kryo, Input input, Class<Consumer> type) {
        Class<? extends Consumer> concreteClass = kryo.readClass(input).getType();
        return kryo.readObject(input, concreteClass);
        //            if (concreteClass == CollideAI.class){
        //                return kryo.readObject(input, CollideAI.class);
        //            } else if (concreteClass == MeleeAI.class){
        //                return kryo.readObject(input, MeleeAI.class);
        //            } else if (concreteClass == RangeAI.class){
        //                return kryo.readObject(input, RangeAI.class);
        //            } else if (concreteClass == PatrouilleWalk.class){
        //                return kryo.readObject(input, PatrouilleWalk.class);
        //            } else if (concreteClass == RadiusWalk.class){
        //                return kryo.readObject(input, RadiusWalk.class);
        //            } else if (concreteClass == StaticRadiusWalk.class){
        //                return kryo.readObject(input, StaticRadiusWalk.class);
        //            } else if (concreteClass == DefaultOnDeath.class){
        //                return kryo.readObject(input, DefaultOnDeath.class);
        //            }
    }
}
