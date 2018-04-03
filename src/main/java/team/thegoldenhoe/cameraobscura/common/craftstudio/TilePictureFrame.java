package team.thegoldenhoe.cameraobscura.common.craftstudio;

import java.awt.image.BufferedImage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.client.ClientPhotoCache;
import team.thegoldenhoe.cameraobscura.common.item.ItemPolaroidSingle;
import team.thegoldenhoe.cameraobscura.common.item.ItemVintagePaper;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoRequest;

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
        //final String pictureLocation = "2018-04-01_23.29.21.png-";
    	String pictureLocation = "";
    	
    	ItemStack held = player.getHeldItem(hand);
    	if (!held.isEmpty() && (held.getItem() instanceof ItemPolaroidSingle || held.getItem() instanceof ItemVintagePaper)) {
    		if (held.getTagCompound() != null) {
    			if (held.getTagCompound().hasKey("Photo")) {
    				pictureLocation = held.getTagCompound().getString("Photo");
    				pictureLocation = pictureLocation.substring(pictureLocation.lastIndexOf('\\') + 1);
    			}
    		}
    	}

        if (world.isRemote) {
            final BufferedImage image = ClientPhotoCache.INSTANCE.getImage(pictureLocation);
            if (image != null) {
                setPicture(pictureLocation);
                setStatus(Status.AVAILABLE);
            } else {
                setPicture(pictureLocation);
                setStatus(Status.REQUEST);
            }
        } else {
            setPicture(pictureLocation);
        }
        return true;
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
