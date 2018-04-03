package team.thegoldenhoe.cameraobscura;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import team.thegoldenhoe.cameraobscura.common.COGuiHandler;
import team.thegoldenhoe.cameraobscura.common.CommonEvents;
import team.thegoldenhoe.cameraobscura.common.CommonProxy;
import team.thegoldenhoe.cameraobscura.common.capability.CameraCapabilities;
import team.thegoldenhoe.cameraobscura.common.craftstudio.BlockFake;
import team.thegoldenhoe.cameraobscura.common.craftstudio.BlockProps;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileTypeMap;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

@Mod(modid = Info.MODID, name = Info.NAME, version = Info.VERSION)
public class CameraObscura {
    public static Logger logger;

    @Mod.Instance(Info.MODID)
    public static CameraObscura instance;

    @SidedProxy(clientSide = Info.CLIENT_PROXY, serverSide = Info.SERVER_PROXY)
    public static CommonProxy proxy;

    public static Block blockProps;
    public static Block blockFake;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit();

        ForgeRegistries.BLOCKS.register(blockProps = new BlockProps().setRegistryName("blockProps"));
        ForgeRegistries.BLOCKS.register(blockFake = new BlockFake().setRegistryName("blockFake"));

        CameraCapabilities.register();
        
        TileTypeMap.register();
        ModelHandler.loadModels();
        
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new COGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CONetworkHandler.init();
        proxy.init();
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
    }

}
