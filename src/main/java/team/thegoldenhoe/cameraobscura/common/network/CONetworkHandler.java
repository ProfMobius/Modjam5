package team.thegoldenhoe.cameraobscura.common.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import team.thegoldenhoe.cameraobscura.Info;

public class CONetworkHandler {
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Info.MODID);

	public static void init() {

	}
}
