package team.thegoldenhoe.cameraobscura.common.network;

import java.util.Comparator;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePhotoDataToServer implements IMessage {

	/** photo name */
	public String name;

	/** photo data */
	public byte[] data;

	/** order used when handling multiple packets of this type */
	public short order;

	/** unique id - used to group multiple messages together */
	public int uuid;

	/** number of bytes total that messages with this uuid should send */
	public int length;
	
	/** uuid of the player that took the photo */
	public String playerUUID;
	
	/** Camera used to take the picture */
	public ItemStack camera;

	public MessagePhotoDataToServer() {
		super();
	}

	public MessagePhotoDataToServer(int uuid, String name, byte[] data, short order, int length, UUID playerUUID, ItemStack camera) {
		this.name = name;
		this.data = data;
		this.uuid = uuid;
		this.order = order;
		this.length = length;
		this.playerUUID = playerUUID.toString();
		this.camera = camera;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.uuid = buf.readInt();
		this.order = buf.readShort();
		this.length = buf.readInt();
		this.name = ByteBufUtils.readUTF8String(buf);
		this.playerUUID = ByteBufUtils.readUTF8String(buf);
		this.camera = ByteBufUtils.readItemStack(buf);
		this.data = new byte[buf.readableBytes()];
		buf.readBytes(this.data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.uuid);
		buf.writeShort(this.order);
		buf.writeInt(this.length);
		ByteBufUtils.writeUTF8String(buf, this.name);
		ByteBufUtils.writeUTF8String(buf, this.playerUUID);
		ByteBufUtils.writeItemStack(buf, this.camera);
		buf.writeBytes(this.data);
	}

	public static final class Handler implements IMessageHandler<MessagePhotoDataToServer, IMessage> {

		@Override
		public IMessage onMessage(MessagePhotoDataToServer message, MessageContext ctx) {
			PhotoDataHandler.bufferMessage(message);
			System.out.println("Num bytes recv:" + message.data.length);
			return null;
		}}

	public static final Comparator<MessagePhotoDataToServer> COMPARATOR = new Comparator<MessagePhotoDataToServer>() {
		@Override
		public int compare(MessagePhotoDataToServer m1, MessagePhotoDataToServer m2) {
			return m1.order - m2.order;
		}
	};
}
