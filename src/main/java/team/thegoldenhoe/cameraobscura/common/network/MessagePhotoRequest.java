package team.thegoldenhoe.cameraobscura.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePhotoRequest implements IMessage {
    private int dimension, x, y, z;
    private String filename;

    public MessagePhotoRequest() {
        super();
    }

    public MessagePhotoRequest(final int dimension, final BlockPos pos, final String filename) {
        this.dimension = dimension;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.filename = filename;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        filename = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, filename);
    }

    public static final class Handler implements IMessageHandler<MessagePhotoRequest, IMessage> {

        @Override
        public IMessage onMessage(final MessagePhotoRequest message, final MessageContext ctx) {
            // TODO : Check if we have the picture on disk and if we do, send it back to the client splitted in multiple packets

            System.out.println(String.format("Requested : %s", message.filename));
            return null;
        }
    }
}
