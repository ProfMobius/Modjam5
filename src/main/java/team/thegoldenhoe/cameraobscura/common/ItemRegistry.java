package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import team.thegoldenhoe.cameraobscura.Info;

@Mod.EventBusSubscriber(modid = Info.MODID)
public class ItemRegistry {

	public static Item camera;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		camera = registerItem(registry, new ItemCamera(), "camera");
	}

	private static Item registerItem(IForgeRegistry<Item> registry, Item item, String name) {
		item.setUnlocalizedName(Info.MODID + "." + name);
		item.setRegistryName(name);
		item.setCreativeTab(CreativeTabs.TOOLS);

		registry.register(item);

		return item;
	}
}
