package team.thegoldenhoe.cameraobscura.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.Info;
import team.thegoldenhoe.cameraobscura.common.CommonProxy;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	/** If true, a photograph will be saved during the RenderTickEvent */
	public static boolean photographPending = false;
	/** Holds the setting for hideGUI so we can restore after changing it to take pics */
	public static boolean hideGUIDefault = Minecraft.getMinecraft().gameSettings.hideGUI;

	@Override
	public void preInit() {

	}

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new ClientEvents());
	}

	/**
	 * https://mcforge.readthedocs.io/en/latest/models/using/#item-models
	 */
	@Override
	public void setModelResourceLocation(Item item, int meta, String name, String variant) {
		if (item != null) {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Info.MODID + ":" + name, variant));	
		}
	}
}
