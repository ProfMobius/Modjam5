package team.thegoldenhoe.cameraobscura.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mia.craftstudio.CSModel;
import com.mia.craftstudio.IPackReaderCallback;
import com.mia.craftstudio.api.ICSProject;
import com.mia.craftstudio.minecraft.forge.CSLibMod;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ProgressManager;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.CameraObscura;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModelHandler {

    public static ICSProject csproject;

    public static Map<Integer, CSModelMetadata> modelData = new HashMap<Integer, CSModelMetadata>() {
        @Override
        public CSModelMetadata get(final Object key) {
            return super.containsKey(key) ? super.get(key) : super.get(0);
        }
    };

    public static void loadModels() {
        csproject = CSLibMod.getCSProjectAndLoad("cspack", null, new IPackReaderCallback() {
            private ProgressManager.ProgressBar progressBar = null;

            @Override
            public void modelLoaded(final CSModel model, final JsonElement json) {
                if (json != null) {
                    progressBar.step(json.getAsJsonObject().get("name").getAsString());
                }
                if (progressBar.getStep() == progressBar.getSteps()) {
                    ProgressManager.pop(progressBar);
                }
            }

            @Override
            public void setCount(final int count) {
                progressBar = ProgressManager.push("Loading models", count);
            }
        });

        final Gson gson = new GsonBuilder().create();
        for (final Map.Entry<Integer, CSModel> entry : csproject.getModels().entrySet()) {
            final JsonElement modelDescriptor = csproject.getDescriptor(entry.getKey());
            if (modelDescriptor != null) {
                validateModelMetadata(gson.fromJson(modelDescriptor, CSModelMetadata.class), entry.getValue());
            }
        }

        CameraObscura.proxy.setupModelWrappers();
    }


    private static void validateModelMetadata(final CSModelMetadata metaData, final CSModel model) {
        if (modelData.containsKey(metaData.decocraftModelID)) { // can't use get because it defaults to creeper for safety
            final CSModelMetadata existingMeta = modelData.get(metaData.decocraftModelID);
            if (!existingMeta.craftstudioAssetName.equals(metaData.craftstudioAssetName)) {
                // throw warning if there is metadata in place already for this deco id and it is a different asset name!
                // means that in a later pack a model was assigned an invalid id
                throw new RuntimeException("You gave a new model the same DecoID as an existing model! Existing:[" + existingMeta.craftstudioAssetName + "], New:[" + metaData.craftstudioAssetName + "]");
            }
        }
        metaData.csmodel = model;
        metaData.validate();
        modelData.put(metaData.decocraftModelID, metaData);
    }

    public static TileEntity getTileEntityPreferNotCreating(final IBlockAccess blockAccess, final BlockPos pos) {
        if (blockAccess instanceof World)
            return getTileEntityWithoutCreating((World) blockAccess, pos);
        else if (blockAccess instanceof ChunkCache)
            return getTileEntityWithoutCreating(((ChunkCache) blockAccess).world, pos);
        else
            return blockAccess.getTileEntity(pos);
    }

    public static TileEntity getTileEntityWithoutCreating(final World world, final BlockPos pos) {
        if (world.isBlockLoaded(pos, false)) // Method is supposed to be quick, so prevent chunk from being created if it does not yet exist
            return getTileEntityUnsafe(pos, world.getChunkFromBlockCoords(pos).getTileEntityMap());
        else
            return null;
    }

    // copied/adapted from previous forge's
    private static TileEntity getTileEntityUnsafe(final BlockPos pos, final Map<BlockPos, TileEntity> tileMap) {
        TileEntity tileentity = tileMap.get(pos);

        if (tileentity != null && tileentity.isInvalid()) {
            tileMap.remove(pos);
            tileentity = null;
        }

        return tileentity;
    }

    public static CSModelMetadata getModelByID(final int id) {
        return modelData.get(id);
    }

    public static CSModelMetadata getModelFromStack(final ItemStack stack) {
        return modelData.get(stack.getItemDamage());
    }

    public static Collection<CSModelMetadata> getAllModelMetadata() {
        return modelData.values();
    }

    public static void addModel(int key, CSModelMetadata data) {
        modelData.put(key, data);
    }

    public static Set<Integer> getAllModelIDs() {
        return modelData.keySet();
    }
}
