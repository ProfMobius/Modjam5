package team.thegoldenhoe.cameraobscura.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.Info;
import team.thegoldenhoe.cameraobscura.common.CommonProxy;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {

	}

	@Override
	public void init() {

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
