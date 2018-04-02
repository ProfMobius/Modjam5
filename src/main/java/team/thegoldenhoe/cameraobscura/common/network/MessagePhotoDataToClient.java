package team.thegoldenhoe.cameraobscura.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.thegoldenhoe.cameraobscura.client.ClientPhotoCache;

public class MessagePhotoDataToClient implements IMessage {
    private String filename;
    private byte[] data;
    private boolean isLast;
    private int fullSize;

    public MessagePhotoDataToClient() {
        super();
    }

    public MessagePhotoDataToClient(final String filename, final byte[] data, final boolean isLast, final int fullSize) {
        this.filename = filename;
        this.data = data;
        this.isLast = isLast;
        this.fullSize = fullSize;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.filename = ByteBufUtils.readUTF8String(buf);
        final int datalength = buf.readInt();
        data = new byte[datalength];
        buf.readBytes(data);
        isLast = buf.readBoolean();
        fullSize = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, filename);
        buf.writeInt(data.length);
        buf.writeBytes(data);
        buf.writeBoolean(isLast);
        buf.writeInt(fullSize);
    }

    public static final class Handler implements IMessageHandler<MessagePhotoDataToClient, IMessage> {

        @Override
        public IMessage onMessage(final MessagePhotoDataToClient message, final MessageContext ctx) {
            ClientPhotoCache.INSTANCE.addBytes(message.filename, message.data, message.isLast, message.fullSize);
            return null;
        }
    }
}
