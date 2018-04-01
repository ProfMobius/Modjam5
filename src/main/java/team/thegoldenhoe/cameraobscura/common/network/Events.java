package team.thegoldenhoe.cameraobscura.common.network;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class Events {

	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		PhotoDataHandler.processMessageQueue();
		PhotoDataHandler.processMessageBuffer();
	}
}
