package team.thegoldenhoe.cameraobscura.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;

public class MessagePhotoRequest implements IMessage {
    private int dim, x, y, z;
    private String location;

    public MessagePhotoRequest() {
        super();
    }

    public MessagePhotoRequest(final int dim, final BlockPos pos, final String location) {
        this.dim = dim;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.location = location;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        dim = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        location = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(dim);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, location);
    }

    public static final class Handler implements IMessageHandler<MessagePhotoRequest, IMessage> {

        @Override
        public IMessage onMessage(final MessagePhotoRequest message, final MessageContext ctx) {
            // TODO : Check if we have the picture on disk and if we do, send it back to the client splitted in multiple packets
            System.out.println(String.format("Requested : %s", message.location));

            final File picture = PhotoDataHandler.getFile(message.location);
            if (picture == null) {
                final MessageFrameStatusUpdate frameUpdateMsg = new MessageFrameStatusUpdate(message.dim, message.x, message.y, message.z, "MISSING");
                CONetworkHandler.NETWORK.sendToAllAround(frameUpdateMsg, new NetworkRegistry.TargetPoint(message.dim, message.x, message.y, message.z, 5 * 16.0));
                return null;
            }


            final MessageFrameStatusUpdate frameUpdateMsg = new MessageFrameStatusUpdate(message.dim, message.x, message.y, message.z, message.location);
            CONetworkHandler.NETWORK.sendToAllAround(frameUpdateMsg, new NetworkRegistry.TargetPoint(message.dim, message.x, message.y, message.z, 5 * 16.0));
            return null;
        }
    }
}
