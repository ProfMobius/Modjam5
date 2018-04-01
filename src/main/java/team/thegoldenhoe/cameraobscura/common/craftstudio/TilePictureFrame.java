package team.thegoldenhoe.cameraobscura.common.craftstudio;

import com.mia.craftstudio.utils.ImageIOCS;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TilePictureFrame extends TileProps {
    private String pictureLocation = "";
    private int glTextureID = 0;

    public void setPicture(final String pictureLocation) {
        if (pictureLocation == this.pictureLocation) {
            return;
        }

        this.pictureLocation = pictureLocation;

        TextureUtil.deleteTexture(glTextureID);
        glTextureID = 0;

        final String dirName = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
        final File directory = new File(dirName, "photographs");
        final File picture = new File(directory, pictureLocation);

        if (!(picture.exists() && picture.isFile())) {
            return;
        }

        try {
            final BufferedImage img = ImageIOCS.read(new FileInputStream(picture));
            glTextureID = TextureUtil.glGenTextures();
            TextureUtil.uploadTextureImage(glTextureID, img);
        } catch (final IOException e) {
            e.printStackTrace();
        }
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
