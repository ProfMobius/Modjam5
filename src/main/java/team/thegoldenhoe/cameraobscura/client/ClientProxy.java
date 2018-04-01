package team.thegoldenhoe.cameraobscura.client;

import com.mia.craftstudio.CSModel;
import com.mia.craftstudio.CraftStudioLib;
import com.mia.craftstudio.minecraft.CraftStudioRendererVBO;
import com.mia.craftstudio.minecraft.ModelMetadata;
import com.mia.craftstudio.minecraft.client.CSClientModelWrapperVBO;
import com.mia.craftstudio.minecraft.client.CSClientModelWrapperVariableVBO;
import com.mia.craftstudio.utils.ImageIOCS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.Info;
import team.thegoldenhoe.cameraobscura.client.renderers.RendererProp;
import team.thegoldenhoe.cameraobscura.common.CommonProxy;
import team.thegoldenhoe.cameraobscura.common.ItemRegistry;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileProps;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;
import team.thegoldenhoe.cameraobscura.utils.SoundRegistry;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy implements IResourceManagerReloadListener {
    private ResourceLocation loading = new ResourceLocation(Info.MODID, "textures/others/loading.png");
    private ResourceLocation missing = new ResourceLocation(Info.MODID, "textures/others/missing.png");


    /**
     * If true, a photograph will be saved during the RenderTickEvent
     */
    public static boolean photographPending = false;
    /**
     * Holds the setting for hideGUI so we can restore after changing it to take pics
     */
    public static boolean hideGUIDefault = Minecraft.getMinecraft().gameSettings.hideGUI;

    @Override
    public void preInit() {
        ClientEvents.register();

        ClientRegistry.bindTileEntitySpecialRenderer(TileProps.class, new RendererProp());
        ForgeHooksClient.registerTESRItemStack(ItemRegistry.itemProps, 0, TileProps.class);
    }

    @Override
    public void init() {
        super.init();

        final ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        for (final Integer key : ModelHandler.getAllModelIDs()) {
            itemModelMesher.register(ItemRegistry.itemProps, key, new ModelResourceLocation("cameraobscura:csitem", "inventory"));
        }
    }

    /**
     * https://mcforge.readthedocs.io/en/latest/models/using/#item-models
     */
    @Override
    public void setModelResourceLocation(final Item item, final int meta, final String name, final String variant) {
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Info.MODID + ":" + name, variant));
        }
    }

    @Override
    public void setupModelWrappers() {
        final ArrayList<ModelMetadata> models = new ArrayList<ModelMetadata>(ModelHandler.getAllModelMetadata());

        final ProgressManager.ProgressBar progressBar = ProgressManager.push("Computing bounding boxes", models.size());

        for (final ModelMetadata modelData : models) {
            if (modelData instanceof CSModelMetadata) {
                progressBar.step(((CSModelMetadata) modelData).name);
            } else {
                progressBar.step(modelData.craftstudioAssetName);
            }

            if (modelData instanceof CSModelMetadata && ((CSModelMetadata) modelData).hasVariableRendering)
                new CSClientModelWrapperVariableVBO(modelData);
            else
                new CSClientModelWrapperVBO(modelData);

            for (final CSModel.ModelNode node : modelData.csmodel.getTopNodes()) {
                ((CSClientModelWrapperVBO) modelData.wrapper).addRenderer(new CraftStudioRendererVBO(modelData.wrapper.nodeCache.get(node)));
            }
        }

        ProgressManager.pop(progressBar);
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
        SoundRegistry.registerAllSounds(ModelHandler.getAllModelMetadata());
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {
        loadResources(resourceManager);
    }

    private void loadResources(final IResourceManager resourceManager) {
        CraftStudioLib.getTimer().reset("Total mipmap");

        final ArrayList<ModelMetadata> models = new ArrayList<ModelMetadata>(ModelHandler.getAllModelMetadata());

        final ProgressManager.ProgressBar progressBar = ProgressManager.push("Processing textures", models.size());

        for (final ModelMetadata modelData : models) {
            if (modelData instanceof CSModelMetadata) {
                progressBar.step(((CSModelMetadata) modelData).name);
            } else {
                progressBar.step(modelData.craftstudioAssetName);
            }

            BufferedImage modelTexture = modelData.csmodel.getTexture();

            //TODO need to make the vbo not be colored if texture if overridden? assume texture is being manually adjusted/painted
            try {
                modelTexture = ImageIOCS.read(resourceManager.getResource(new ResourceLocation(Info.MODID.toLowerCase(), String.format("textures/models/%s", modelData.textureOverride))).getInputStream());
            } catch (final Exception e) {
                // Silently ignore all errors, if the resource is not found, use the baked in one from the cspack;
            }

            // Adapted some code from Textureutil
            ((CSClientModelWrapperVBO) modelData.wrapper).deleteGlTexture();
            ((CSClientModelWrapperVBO) modelData.wrapper).bindGlTexture();

            final int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
            //int mipmapLevels = 0;	// We desactivate mipmap computation for now since it is taking most of the loading time.

            if (mipmapLevels > 0) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipmapLevels);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0.0F);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, mipmapLevels);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
            }

            for (int lvl = 0; lvl <= mipmapLevels; ++lvl) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, lvl, GL11.GL_RGBA, modelTexture.getWidth() >> lvl, modelTexture.getHeight() >> lvl, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
            }

            final int[][] imgArray = new int[1 + mipmapLevels][];
            imgArray[0] = modelTexture.getRGB(0, 0, modelTexture.getWidth(), modelTexture.getHeight(), null, 0, modelTexture.getWidth());

            CraftStudioLib.getTimer().start("mipmap");
            // This is the part taking a shittone of time. It is actually cacheable since it is just a giant array of int[][] <= That's 300 MB
            final int[][] mipdata = ImageIOCS.generateMipmapData(mipmapLevels, modelTexture.getWidth(), imgArray);
            CraftStudioLib.getTimer().stop("mipmap");
            CraftStudioLib.getTimer().add("Total mipmap", "mipmap");

            TextureUtil.uploadTextureMipmap(mipdata, modelTexture.getWidth(), modelTexture.getHeight(), 0, 0, false, false);
        }
        ProgressManager.pop(progressBar);
        CraftStudioLib.getTimer().printAcc("Total mipmap");
    }

    @Override
    public int getPhotographGLId(final int oldID, final String pictureLocation) {
        TextureUtil.deleteTexture(oldID);
        int glID = 0;

        final String dirName = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
        final File directory = new File(dirName, "photographs");
        final File picture = new File(directory, pictureLocation);

        BufferedImage img = null;

        if ("MISSING".equals(pictureLocation)) {
            try {
                img = ImageIOCS.read(Minecraft.getMinecraft().getResourceManager().getResource(missing).getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (picture.exists() && picture.isFile()) {
            try {
                img = ImageIOCS.read(new FileInputStream(picture));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                img = ImageIOCS.read(Minecraft.getMinecraft().getResourceManager().getResource(loading).getInputStream());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }


        if (img != null) {
            glID = TextureUtil.glGenTextures();
            TextureUtil.uploadTextureImage(glID, img);
        }

        return glID;
    }
}
