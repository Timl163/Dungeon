package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.packages.request.UpdateOwnPositionRequest;
import tools.Point;

public class UpdateOwnPositionRequestSerializer extends Serializer<UpdateOwnPositionRequest> {
    @Override
    public void write(Kryo kryo, Output output, UpdateOwnPositionRequest object) {
        output.writeInt(object.getPlayerId());
        kryo.writeObject(output, object.getPosition());
    }

    @Override
    public UpdateOwnPositionRequest read(Kryo kryo, Input input, Class<UpdateOwnPositionRequest> type) {
        int playerId = input.readInt();
        Point position = kryo.readObject(input, Point.class);
        return new UpdateOwnPositionRequest(playerId, position);
    }
}