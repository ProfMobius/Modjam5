package team.thegoldenhoe.cameraobscura.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.IOUtils;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TilePictureFrame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

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
                final MessageFrameStatusUpdate frameUpdateMsg = new MessageFrameStatusUpdate(message.dim, message.x, message.y, message.z, message.location, TilePictureFrame.Status.MISSING);
                CONetworkHandler.NETWORK.sendToDimension(frameUpdateMsg, message.dim);
                return null;
            }

            try {
                byte[] bytes = IOUtils.toByteArray(new FileInputStream(picture));
                int length = bytes.length;
                int index = 0;
                int maxPacketSize = 30000;
                while (true) {
                    int remainingData = length - index;
                    boolean isLast = remainingData <= maxPacketSize;
                    int packetSize = Math.min(maxPacketSize, remainingData);
                    byte[] outData = Arrays.copyOfRange(bytes, index, index + packetSize);
                    index += packetSize;

                    final MessagePhotoDataToClient msg = new MessagePhotoDataToClient(message.location, outData, isLast, length);
                    CONetworkHandler.NETWORK.sendToDimension(msg, message.dim);

                    if (isLast) {
                        break;
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            final MessageFrameStatusUpdate frameUpdateMsg = new MessageFrameStatusUpdate(message.dim, message.x, message.y, message.z, message.location, TilePictureFrame.Status.AVAILABLE);
            CONetworkHandler.NETWORK.sendToDimension(frameUpdateMsg, message.dim);
            return null;
        }
    }
}
