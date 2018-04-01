package team.thegoldenhoe.cameraobscura.common.craftstudio;

import net.minecraft.nbt.NBTTagCompound;
import team.thegoldenhoe.cameraobscura.CameraObscura;

public class TilePictureFrame extends TileProps {
    private String pictureLocation = "";
    private int glTextureID = 0;

    public void setPicture(final String pictureLocation) {
        if (this.pictureLocation.equals(pictureLocation)) {
            return;
        }

        this.pictureLocation = pictureLocation;
        glTextureID = CameraObscura.proxy.getPhotographGLId(glTextureID, pictureLocation);
    }

    public int getGlTextureID() {
        return glTextureID;
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
