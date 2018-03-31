package team.thegoldenhoe.cameraobscura;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import team.thegoldenhoe.cameraobscura.common.CommonProxy;

@Mod(modid = Info.MODID, name = Info.NAME, version = Info.VERSION)
public class CameraObscura {

	public static Logger logger;

	@Mod.Instance(Info.MODID)
	public static CameraObscura instance;

	@SidedProxy(clientSide = Info.CLIENT_PROXY, serverSide = Info.SERVER_PROXY)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
	}
}
