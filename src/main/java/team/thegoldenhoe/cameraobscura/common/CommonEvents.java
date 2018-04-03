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
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.common.item.ItemRegistry;
import team.thegoldenhoe.cameraobscura.common.network.PhotoDataHandler;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

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
			
			if (!heldMain.isEmpty()) {
				if (heldMain.getItem() == ItemRegistry.itemProps) {
					CSModelMetadata data = ModelHandler.getModelFromStack(heldMain);
					if (data.placeable) {
						// If placeable, we want to place, so let's exit
						return;
					} else {
						event.setCanceled(true);
					}
				}
			} else if (!heldOff.isEmpty()) {
				if (heldOff.getItem() == ItemRegistry.itemProps) {
					CSModelMetadata data = ModelHandler.getModelFromStack(heldMain);
					if (data.placeable) {
						// If placeable, we want to place, so let's exit
						return;
					} else {
						event.setCanceled(true);
					}
				} else {
					return;
				}
			}
		}
	}
}
