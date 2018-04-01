package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.item.Item;

public class CommonProxy {
	public void preInit() {}
	public void init() {}

	/**
	 * https://mcforge.readthedocs.io/en/latest/models/using/#item-models
	 */
	public void setModelResourceLocation(Item item, int meta, String name, String variant) {}
}
