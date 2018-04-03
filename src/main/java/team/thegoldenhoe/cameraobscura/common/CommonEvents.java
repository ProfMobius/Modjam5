package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServerMulti;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import team.thegoldenhoe.cameraobscura.Utils;
import team.thegoldenhoe.cameraobscura.common.network.PhotoDataHandler;

public class CommonEvents {

	@SubscribeEvent
	public void tickServerWorld(WorldTickEvent event) {
		if (event.side == Side.SERVER && !(event.world instanceof WorldServerMulti)) {
			PhotoDataHandler.processMessageQueue();
			PhotoDataHandler.processMessageBuffer(event.world);	
		}
	}

	@SubscribeEvent
	public void cancelEntityInteractionSpecific(EntityInteractSpecific event) {
		cancelClick(event.getEntityPlayer(), event);
	}

	@SubscribeEvent
	public void cancelEntityInteraction(EntityInteract event) {
		cancelClick(event.getEntityPlayer(), event);
	}

	@SubscribeEvent
	public void cancelRightClickBlock(RightClickBlock event) {
		cancelClick(event.getEntityPlayer(), event);
	}

	private void cancelClick(EntityPlayer player, Event event) {
		if (player != null) {
			ItemStack heldMain = player.getHeldItemMainhand();
			ItemStack heldOff = player.getHeldItemOffhand();

			if (Utils.isCamera(heldMain) || Utils.isCamera(heldOff)) {
				event.setCanceled(true);
			}
		}
	}
}
