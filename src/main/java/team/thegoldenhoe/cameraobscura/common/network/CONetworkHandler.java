package team.thegoldenhoe.cameraobscura.common.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import team.thegoldenhoe.cameraobscura.Info;

public class CONetworkHandler {
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Info.MODID);

	public static void init() {
		NETWORK.registerMessage(MessagePhotoData.Handler.class, MessagePhotoData.class, 0, Side.SERVER);
		NETWORK.registerMessage(MessagePhotoRequest.Handler.class, MessagePhotoRequest.class, 1, Side.SERVER);
	}
}
