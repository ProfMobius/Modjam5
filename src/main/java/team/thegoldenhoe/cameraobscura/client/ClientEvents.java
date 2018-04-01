package team.thegoldenhoe.cameraobscura.client;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ClientEvents {

	@SubscribeEvent
	public void cancelRenderOverlay(RenderGameOverlayEvent.Pre event) {
		if (ClientProxy.cancelHUDRendering) {
			event.setCanceled(true);
		} else {
			
		}
	}
}
