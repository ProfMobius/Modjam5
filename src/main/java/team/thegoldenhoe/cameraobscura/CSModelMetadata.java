package team.thegoldenhoe.cameraobscura;

import com.google.gson.*;
import com.mia.craftstudio.CSModel.ModelNode.Attrb;
import com.mia.craftstudio.libgdx.Vector3;
import com.mia.craftstudio.minecraft.ModelMetadata;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileTypeMap;
import team.thegoldenhoe.cameraobscura.common.network.CameraTypes;
import team.thegoldenhoe.cameraobscura.utils.JsonHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CSModelMetadata extends ModelMetadata {
    public boolean showOutline = true;
    public String name;
    public int decocraftModelID;
    public int lightLevel = 0;
    public int invX = 0;
    public int invY = 0;
    public boolean walkthrough = false;
    public boolean colorable = false;
    public boolean limitRotation = false;
    public boolean hasVariableRendering = false;
    public boolean spiderweb = false;
    public boolean placeable = true;
    public TabProps tab = TabProps.Main;
    public TileTypeMap tileType = TileTypeMap.Props;
    public String sound;
    public String recipe;
    public int placedModel = -1;
    public boolean creativeInv = true;

    public Map<Integer, JsonObject> variants = null;

    public transient float itemScale = -1;
    public transient Vector3 itemOffset;

    private transient Map<Integer, AxisAlignedBB> aabbs = new HashMap<Integer, AxisAlignedBB>();

    public CSModelMetadata() {
    }

    // Small map cache to speed up later access times
    private static final HashMap<String, Field> fieldMap = new HashMap<String, Field>();

    {
        if (fieldMap.isEmpty()) {
            for (final Field f : CSModelMetadata.class.getFields()) {
                fieldMap.put(f.getName(), f);
            }
        }
    }

    // Copy constructor with optional JSON overrides
    public CSModelMetadata(final CSModelMetadata existingMeta, final int newID, final JsonObject changes) {
        this.craftstudioAssetName = existingMeta.craftstudioAssetName;
        this.craftstudioAssetID = existingMeta.craftstudioAssetID;
        this.scale = existingMeta.scale;
        this.textureOverride = null;
        this.tileParams = new HashMap<String, String>(existingMeta.tileParams);
        this.csmodel = existingMeta.csmodel;
        this.name = existingMeta.name;
        this.decocraftModelID = newID;
        this.lightLevel = existingMeta.lightLevel;
        this.invX = existingMeta.invX;
        this.invY = existingMeta.invY;
        this.walkthrough = existingMeta.walkthrough;
        this.colorable = existingMeta.colorable;
        this.limitRotation = existingMeta.limitRotation;
        this.hasVariableRendering = existingMeta.hasVariableRendering;
        this.spiderweb = existingMeta.spiderweb;
        this.tab = existingMeta.tab;
        this.tileType = existingMeta.tileType;
        this.sound = existingMeta.sound;
        this.recipe = null;
        this.variants = null;
        this.itemScale = existingMeta.itemScale;
        this.itemOffset = existingMeta.itemOffset != null ? new Vector3(existingMeta.itemOffset) : null;
        this.showOutline = existingMeta.showOutline;

        if (changes != null) {
            final Gson gson = new GsonBuilder().create();
            for (final Entry<String, JsonElement> entry : changes.entrySet()) {
                final Field foundField = fieldMap.get(entry.getKey().intern());
                if (foundField != null) {
                    try {
                        foundField.set(this, gson.fromJson(entry.getValue(), foundField.getGenericType()));
                    } catch (final JsonSyntaxException e) {
                    } catch (final IllegalArgumentException e) {
                    } catch (final IllegalAccessException e) {
                    }
                }
            }
        }
    }

    @Override
    public void wrapperCallback() {
        super.wrapperCallback();

        final int[] extendBlock = this.wrapper.getExtendBlock(0);
        final int width = (1 + extendBlock[3] - extendBlock[0]);
        final int depth = (1 + extendBlock[5] - extendBlock[2]);
        // Not always adding +1 here due to models going below ground...
        int height = (extendBlock[4] - extendBlock[1]);
        if (height == 0) {
            height++;
        }

        //Calculate item render scale and offset:  used for in world floating items, held in hand items, and gui
        final Vector3[] extend = this.wrapper.getExtend(8);
        itemScale = Math.min((1f / Math.abs(extend[1].y - extend[0].y)), Math.min((1f / Math.abs(extend[1].x - extend[0].x)), (1f / Math.abs(extend[1].z - extend[0].z))));
        itemOffset = new Vector3(
                -((extend[0].x + extend[1].x) / 2f),
                -(((extend[0].y + extend[1].y) / 35f) + (extend[0].y < 0f ? extend[0].y / 2 : 0) + (extend[0].y > 0f ? extend[0].y : 0) + (extend[1].y > .5f ? extend[1].y / 35 : 0) + .84f),
                -((extend[0].z + extend[1].z) / 2.2f));
    }

    // registration will ensure validity of recipe and register the recipe in the cultivation recipe collision set and register with MC
    public void validate() {
        // Texture Override
        if (this.textureOverride == null) {
            this.textureOverride = this.getDefaultTextureName();
        }

        if (this.csmodel.getRootNode() != null && this.csmodel.getRootNode().hasAttribute(Attrb.PASSABLEPROPAGATES)) {
            // if the root node is propagating a passable value, set this here and save some logic in the collision code
            this.walkthrough = true;
        }
    }

    public String getDefaultTextureName() {
        return String.format("%04d_%s.png", this.decocraftModelID, this.name.toLowerCase().replaceAll("[/\\\\:*?\"<>|' ]", "_"));
    }

    public CameraTypes getCameraType() {
        String cameraType = tileParams.get("cameraType");
        if (cameraType == null) {
            return CameraTypes.NOT_A_CAMERA;
        }

        return CameraTypes.valueOf(cameraType);
    }

    public boolean isBlockInList(final IBlockState state, final String key) {
        final String blockListString = tileParams.getOrDefault(key, "any");

        if ("any".equals(blockListString)) {
            return true;
        }

        if ("none".equals(blockListString) || blockListString.isEmpty()) {
            return false;
        }

        return JsonHelper.blocksFromString(blockListString).contains(state);
    }

    @Override
    public int compareTo(final ModelMetadata o) {
        if (o instanceof CSModelMetadata) {
            if (this.decocraftModelID < ((CSModelMetadata) o).decocraftModelID)
                return -1;
            if (this.decocraftModelID > ((CSModelMetadata) o).decocraftModelID)
                return 1;
            return 0;
        } else {
            return super.compareTo(o);
        }
    }

    public AxisAlignedBB getBoundingBox(final int rotation) {
        //Create an AABB out of the corresponding extend for a given rotation. Caches the result to prevent abusive creation of AABBs
        AxisAlignedBB aabb = aabbs.get(rotation);
        if (aabb == null) {
            final Vector3[] extend = wrapper.getExtend(rotation);
            aabb = new AxisAlignedBB(extend[0].x, extend[0].y, extend[0].z, extend[1].x, extend[1].y, extend[1].z);
            aabbs.put(rotation, aabb);
            return aabb;
        }
        return aabb;
    }
}
