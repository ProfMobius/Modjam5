package team.thegoldenhoe.cameraobscura.common.craftstudio;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.client.ClientPhotoCache;
import team.thegoldenhoe.cameraobscura.common.capability.CameraCapabilities;
import team.thegoldenhoe.cameraobscura.common.capability.ICameraStorageNBT;
import team.thegoldenhoe.cameraobscura.common.item.ItemBrush;
import team.thegoldenhoe.cameraobscura.common.item.ItemPolaroidSingle;
import team.thegoldenhoe.cameraobscura.common.item.ItemSDCard;
import team.thegoldenhoe.cameraobscura.common.item.ItemVintagePaper;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoRequest;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TilePictureFrame extends TileProps implements ITickable {
    public enum Status {
        EMPTY,
        MISSING,
        REQUEST,
        LOADING,
        AVAILABLE,
        SERVER
    }

    private String prevLocation = "";
    private String pictureLocation = "";
    private Status prevStatus = Status.EMPTY;
    private Status status = Status.EMPTY;
    private int glTextureID = 0;

    public void setPicture(final String pictureLocation) {
        this.pictureLocation = pictureLocation;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getPictureLocation() {
        return pictureLocation;
    }

    public int getGlTextureID() {
        return glTextureID;
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        String newPictureLocation = "";

        ItemStack held = player.getHeldItem(hand);
        if (!held.isEmpty() && held.getItem() instanceof ItemBrush) {
            final CSModelMetadata modelData = ModelHandler.getModelByID(type);
            final int nextModelIndex = modelData.tileParams.containsKey("nextModel") ? Integer.valueOf(modelData.tileParams.get("nextModel")) : -1;

            if (nextModelIndex < 0) {
                return true;
            }

            switchModel(nextModelIndex);

            return true;
        }

        if (!held.isEmpty() && held.getItem() instanceof ItemSDCard) {
            if (held.getTagCompound() != null) {
                final ICameraStorageNBT.SDCardStorage storage = held.getCapability(CameraCapabilities.getSDCardStorageCapability(), null);
                final ArrayList<String> paths = storage.getSavedImagePaths();

                int nextPictureIndex = 0;
                for (int i = 0; i < paths.size(); i++) {
                    if (cleanPath(paths.get(i)).equals(pictureLocation)) {
                        nextPictureIndex = i + 1;
                        break;
                    }
                }

                if (nextPictureIndex >= paths.size()) {
                    nextPictureIndex = 0;
                }

                newPictureLocation = cleanPath(paths.get(nextPictureIndex));
            }
        }

        if (!held.isEmpty() && (held.getItem() instanceof ItemPolaroidSingle || held.getItem() instanceof ItemVintagePaper)) {
            if (held.getTagCompound() != null && held.getTagCompound().hasKey("Photo")) {
                newPictureLocation = cleanPath(held.getTagCompound().getString("Photo"));
            }
        }

        if (world.isRemote) {
            final BufferedImage image = ClientPhotoCache.INSTANCE.getImage(newPictureLocation);
            if (image != null) {
                setPicture(newPictureLocation);
                setStatus(Status.AVAILABLE);
            } else {
                setPicture(newPictureLocation);
                setStatus(Status.REQUEST);
            }
        } else {
            setPicture(newPictureLocation);
        }
        return true;
    }

    private String cleanPath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        } else {
            return path.substring(path.lastIndexOf('\\') + 1);
        }
    }

    private void switchModel(final int newModelID) {
        final CSModelMetadata targetData = ModelHandler.getModelByID(newModelID);
        final CSModelMetadata currentData = ModelHandler.getModelByID(type);

        if (!TileProps.canReplace(targetData.wrapper, world, pos.getX(), pos.getY(), pos.getZ(), false, rotation, currentData.wrapper.getExtendPlacementBlock(rotation))) {
            return;
        }

        removeSlaves();
        world.setBlockToAir(pos);
        world.setBlockState(pos, CameraObscura.blockProps.getDefaultState().withProperty(BlockProps.FACING, EnumFacing.NORTH));

        final TileProps tileProps = TileProps.checkAndGetTileEntity(world, pos, newModelID);

        tileProps.type = newModelID;
        tileProps.rotation = rotation;
        tileProps.tileParams = targetData.tileParams;

        if (tileProps instanceof TilePictureFrame) {
            TilePictureFrame tileFrame = (TilePictureFrame) tileProps;
            tileFrame.prevStatus = status;
            tileFrame.prevLocation = prevLocation;
            tileFrame.status = status;
            tileFrame.pictureLocation = pictureLocation;
        }

        tileProps.init();
        tileProps.blockPlaced(world.getBlockState(pos), world, pos);
        tileProps.createSlaves();

        tileProps.markDirty();
        world.checkLight(pos);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setString("pictureLocation", pictureLocation);

        return tagCompound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        setPicture(tagCompound.getString("pictureLocation"));
        setStatus(Status.REQUEST);
    }

    @Override
    public void update() {
        if (world.isRemote && (!prevLocation.equals(pictureLocation) || prevStatus != status)) {
            prevLocation = pictureLocation;
            prevStatus = status;

            if (!"".equals(pictureLocation) && status == Status.REQUEST) {
                setStatus(Status.LOADING);
                CONetworkHandler.NETWORK.sendToServer(new MessagePhotoRequest(world.provider.getDimension(), pos, pictureLocation));
            }

            float aspectRatio = tileParams.containsKey("aspectRatio") ? Float.valueOf(tileParams.get("aspectRatio")) : 1.0f;
            glTextureID = CameraObscura.proxy.uploadPictureToGPU(glTextureID, pictureLocation, status, aspectRatio);
        }
    }
}
