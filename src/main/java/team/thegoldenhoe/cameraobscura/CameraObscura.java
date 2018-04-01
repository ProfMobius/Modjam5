package team.thegoldenhoe.cameraobscura;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import team.thegoldenhoe.cameraobscura.common.CommonEvents;
import team.thegoldenhoe.cameraobscura.common.CommonProxy;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileTypeMap;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

import java.util.HashMap;
import java.util.Map;

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

        TileTypeMap.register();
        ModelHandler.loadModels();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CONetworkHandler.init();
        proxy.init();
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
    }

}
