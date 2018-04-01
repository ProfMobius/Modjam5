package team.thegoldenhoe.cameraobscura.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.Info;

@SideOnly(Side.CLIENT)
public class ClientUtils {
	public static ResourceLocation bindTextureGui(String path) {
		ResourceLocation loc = new ResourceLocation(Info.MODID, String.format("textures/gui/%s.png", path));
		Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
		return loc;
	}
}
