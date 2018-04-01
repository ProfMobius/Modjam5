package team.thegoldenhoe.cameraobscura.client.renderers;

import com.mia.craftstudio.libgdx.Vector3;
import com.mia.craftstudio.minecraft.client.CSClientModelWrapperVBO;
import com.mia.craftstudio.minecraft.forge.CSLibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.MinecraftForgeClient;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileProps;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

public class RendererProp extends TileEntitySpecialRenderer<TileProps> {
    private static final Vector3 halfBlock = new Vector3(0.5f, 0, 0.5f);
    private static final Vector3 neghalfBlock = new Vector3(-0.5f, 0, -0.5f);

    @Override
    public void render(TileProps tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        int renderPass = MinecraftForgeClient.getRenderPass();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z); // move rendering to TE rendering Offset XYZ

        boolean showOutline = false;
        RayTraceResult target = Minecraft.getMinecraft().objectMouseOver;
        if (CSLibMod.displayOutline && tile != null && target != null && (target.typeOfHit == RayTraceResult.Type.BLOCK && target.getBlockPos().equals(tile.getPos()))) {
            showOutline = true;
        }

        if (tile != null) {
            ((CSClientModelWrapperVBO) ModelHandler.getModelByID(tile.getRenderingType()).wrapper).render(tile, partialTicks, renderPass, showOutline, (tile.rotation * 22.5F), Vector3.invY, halfBlock, neghalfBlock);
        }

        GlStateManager.popMatrix();

        String greenScreen = tile.tileParams.get("greenScreen");
        if (tile != null && greenScreen != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z); // move rendering to TE rendering Offset XYZ
            CSClientModelWrapperVBO wrapper = (CSClientModelWrapperVBO) ModelHandler.getModelByID(Integer.valueOf(greenScreen)).wrapper;
            wrapper.render(tile, partialTicks, renderPass, showOutline, (tile.rotation * 22.5F), Vector3.invY, halfBlock, neghalfBlock);
            GlStateManager.popMatrix();
        }
    }
}
