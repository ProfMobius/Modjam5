package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import team.thegoldenhoe.cameraobscura.common.network.PhotoDataHandler;

public class CommonEvents {

	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		PhotoDataHandler.processMessageQueue();
		PhotoDataHandler.processMessageBuffer();
	}

	@SubscribeEvent
	public void cancelEntityInteractionSpecific(EntityInteractSpecific event) {
		//cancelClick(event.getEntityPlayer(), event);
	}

	@SubscribeEvent
	public void cancelEntityInteraction(EntityInteract event) {
		//cancelClick(event.getEntityPlayer(), event);
	}

	@SubscribeEvent
	public void cancelRightClickBlock(RightClickBlock event) {
		//cancelClick(event.getEntityPlayer(), event);
	}

	private void cancelClick(EntityPlayer player, Event event) {
		if (player != null) {
			ItemStack held = player.getHeldItemMainhand();
			if (!held.isEmpty() && held.getItem() == ItemRegistry.camera) {
				event.setCanceled(true);
			}
		}
	}
}
