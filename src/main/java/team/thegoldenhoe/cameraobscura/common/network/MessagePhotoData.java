package team.thegoldenhoe.cameraobscura.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePhotoData implements IMessage {

	/** photo name */
	private String name;

	/** photo data */
	private byte[] data;

	public MessagePhotoData() {
		super();
	}

	public MessagePhotoData(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		this.data = new byte[buf.readableBytes()];
		buf.readBytes(this.data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.name);
		buf.writeBytes(this.data);
	}

	public static final class Handler implements IMessageHandler<MessagePhotoData, IMessage> {

		@Override
		public IMessage onMessage(MessagePhotoData message, MessageContext ctx) {
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			// temporary, obviously...or is it?
			serverPlayer.getServerWorld().addScheduledTask(() -> {
				serverPlayer.inventory.addItemStackToInventory(new ItemStack(Items.DIAMOND, 1));
			});
			return null;
		}}
}
