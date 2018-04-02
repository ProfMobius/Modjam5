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
	public static Item sdCard;
	public static Item photograph;

	// cameras
	public static Item cameraPolaroid;
	public static Item cameraOldFashioned;
	public static Item cameraDigital;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		camera = registerItem(registry, new ItemCamera(), "camera");
		itemProps = registerItem(registry, new ItemProps(), "csitem");
		filter = registerMultiItem(registry, new ItemFilter(), "filter", "filter_sepia", "filter_gloomy", "filter_happy", "filter_retro", "filter_high_contrast");
		sdCard = registerItem(registry, new ItemSDCard(), "sdcard");
		photograph = registerItem(registry, new ItemPhotograph(), "photograph");
		
		cameraPolaroid = registerItem(registry, new ItemCamera(), "camera_polaroid");
		cameraOldFashioned = registerItem(registry, new ItemCamera(), "camera_old_fashioned");
		cameraDigital = registerItem(registry, new ItemCamera(), "camera_digital");
	}

	private static <I extends Item> I registerMultiItem(IForgeRegistry<Item> registry, I item, String name, String... variantNames) {
		I ret = registerItem(registry, item, name, variantNames[0]);

		for (int i = 1; i < variantNames.length; i++) {
			CameraObscura.proxy.setModelResourceLocation(item, i, variantNames[i], "inventory");
		}

		return ret;
	}

	private static <I extends Item> I registerItem(IForgeRegistry<Item> registry, I item, String name, String variantName) {
		item.setUnlocalizedName(Info.MODID + "." + name);
		item.setRegistryName(name);
		item.setCreativeTab(CreativeTabs.TOOLS);

		registry.register(item);
		CameraObscura.proxy.setModelResourceLocation(item, 0, variantName, "inventory");
		return item;
	}

	private static <I extends Item> I registerItem(IForgeRegistry<Item> registry, I item, String name) {
		return registerItem(registry, item, name, "inventory");
	}
}
