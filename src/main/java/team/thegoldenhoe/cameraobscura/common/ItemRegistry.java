package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.Info;

@Mod.EventBusSubscriber(modid = Info.MODID)
public class ItemRegistry {

	public static Item camera;
	public static Item itemProps;
	public static Item filter;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		camera = registerItem(registry, new ItemCamera(), "camera");
		itemProps = registerItem(registry, new ItemProps(), "csitem");
		filter = registerMultiItem(registry, new ItemFilter(), "filter", "sepia");
	}

	private static <I extends Item> I registerMultiItem(IForgeRegistry<Item> registry, I item, String name, String... variantNames) {
		I ret = registerItem(registry, item, name, variantNames[0]);

		for (int i = 1; i < variantNames.length; i++) {
			CameraObscura.proxy.setModelResourceLocation(item, i, name, variantNames[i]);
		}

		return ret;
	}

	private static <I extends Item> I registerItem(IForgeRegistry<Item> registry, I item, String name, String variantName) {
		item.setUnlocalizedName(Info.MODID + "." + name);
		item.setRegistryName(name);
		item.setCreativeTab(CreativeTabs.TOOLS);

		registry.register(item);
		CameraObscura.proxy.setModelResourceLocation(item, 0, name, variantName);
		return item;
	}

	private static <I extends Item> I registerItem(IForgeRegistry<Item> registry, I item, String name) {
		item.setUnlocalizedName(Info.MODID + "." + name);
		item.setRegistryName(name);
		item.setCreativeTab(CreativeTabs.TOOLS);

		registry.register(item);
		CameraObscura.proxy.setModelResourceLocation(item, 0, name, "inventory");
		return item;
	}
}
