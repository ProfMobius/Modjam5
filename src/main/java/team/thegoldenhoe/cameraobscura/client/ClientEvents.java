package team.thegoldenhoe.cameraobscura.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;


public class ClientEvents {

	@SubscribeEvent
	public void capturePhotographs(RenderTickEvent event) {
		// Check if:
		// - A photograph is pending capture
		// - Gui is hidden
		// - It is the end phase of the render tick. If it's not the end phase, the gui
		//   will still be showing since the buffer hasn't been cleared yet
		if (ClientProxy.photographPending && Minecraft.getMinecraft().gameSettings.hideGUI
				&& event.phase == TickEvent.Phase.END) {
			PhotographHelper.capturePhotograph(PhotoFilters.VINTAGE);
			// Restore hide gui setting to whatever it was before
			Minecraft.getMinecraft().gameSettings.hideGUI = ClientProxy.hideGUIDefault;
			ClientProxy.photographPending = false;
		}
	}
}
