package team.thegoldenhoe.cameraobscura.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TilePictureFrame;

public class MessageFrameStatusUpdate implements IMessage {
    private int dim;
    private int x;
    private int y;
    private int z;
    private String location;

    public MessageFrameStatusUpdate() {
        super();
    }

    public MessageFrameStatusUpdate(final int dim, final int x, final int y, final int z, final String location) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.location = location;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dim = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        location = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dim);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, location);
    }

    public static final class Handler implements IMessageHandler<MessageFrameStatusUpdate, IMessage> {

        @Override
        public IMessage onMessage(final MessageFrameStatusUpdate message, final MessageContext ctx) {
            final World world = CameraObscura.proxy.getClientWorld();

            if (world == null) {
                return null;
            }

            final TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
            if (tileEntity instanceof TilePictureFrame) {
                final TilePictureFrame frame = (TilePictureFrame) tileEntity;
                frame.setPicture(message.location);
            }

            return null;
        }
    }


}
