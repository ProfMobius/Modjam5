package team.thegoldenhoe.cameraobscura.common.craftstudio;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.client.ClientPhotoCache;
import team.thegoldenhoe.cameraobscura.common.network.CONetworkHandler;
import team.thegoldenhoe.cameraobscura.common.network.MessagePhotoRequest;

import java.awt.image.BufferedImage;

public class TilePictureFrame extends TileProps {
    private String pictureLocation = "";
    private int glTextureID = 0;

    public void setPicture(final String pictureLocation) {
        this.pictureLocation = pictureLocation;
        glTextureID = CameraObscura.proxy.getPhotographGLId(glTextureID, pictureLocation);
    }

    public int getGlTextureID() {
        return glTextureID;
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final String pictureLocation = "2018-04-01_23.29.21.png";

        if (world.isRemote) {
            final BufferedImage image = ClientPhotoCache.INSTANCE.getImage(pictureLocation);
            if (image != null) {
                setPicture(pictureLocation);
            } else {
                setPicture("LOADING");
                CONetworkHandler.NETWORK.sendToServer(new MessagePhotoRequest(world.provider.getDimension(), pos, pictureLocation));
            }
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
    }
}
