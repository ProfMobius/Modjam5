package team.thegoldenhoe.cameraobscura.common.item;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.Info;
import team.thegoldenhoe.cameraobscura.TabProps;

@Mod.EventBusSubscriber(modid = Info.MODID)
public class ItemRegistry {

	private static final String[] filterNames = new String[] {
			"filter_sepia", "filter_gloomy", "filter_happy", "filter_retro", "filter_high_contrast", "filter_low_sobel", "filter_high_sobel"
	};

	public static Item itemProps;
	public static Item filter;
	public static Item sdCard;
	public static Item brush;
	public static Item polaroidStack;
	public static Item vintagePhoto;
	public static Item polaroidSingle;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		itemProps = registerItem(registry, new ItemProps(), "csitem", false);
		filter = registerMultiItem(registry, new ItemFilter(), "filter", filterNames);
		sdCard = registerItem(registry, new ItemSDCard(), "sdcard", "sdcard", true);
		brush = registerItem(registry, new ItemBrush(), "brush", "brush", false);
		polaroidStack = registerItem(registry, new ItemPolaroidStack(), "polaroid_stack", "polaroid_stack", true);
		polaroidSingle = registerItem(registry, new ItemPolaroidSingle(), "polaroid_photo", "polaroid_photo", false);
		vintagePhoto = registerItem(registry, new ItemVintagePaper(), "vintage_photo", "vintage_photo", true);
	}

	private static <I extends Item> I registerMultiItem(IForgeRegistry<Item> registry, I item, String name, String... variantNames) {
		I ret = registerItem(registry, item, name, variantNames[0], false);

		for (int i = 1; i < variantNames.length; i++) {
			CameraObscura.proxy.setModelResourceLocation(item, i, variantNames[i], "inventory");
		}

		return ret;
	}

	private static <I extends Item> I registerItem(IForgeRegistry<Item> registry, I item, String name, String variantName, boolean setTab) {
		item.setUnlocalizedName(Info.MODID + "." + name);
		item.setRegistryName(name);
		if (setTab) item.setCreativeTab(TabProps.Main.get());

		registry.register(item);
		CameraObscura.proxy.setModelResourceLocation(item, 0, variantName, "inventory");
		return item;
	}

	private static <I extends Item> I registerItem(IForgeRegistry<Item> registry, I item, String name, boolean setTab) {
		return registerItem(registry, item, name, "inventory", setTab);
	}
}
