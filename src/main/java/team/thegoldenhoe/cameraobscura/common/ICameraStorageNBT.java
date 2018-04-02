package team.thegoldenhoe.cameraobscura.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICameraStorageNBT extends INBTSerializable<NBTTagCompound> {

	/** Returns paths to each of the images saved on this storage item */
	String[] getSavedImagePaths();
	/** Override the current list of saved paths with a new one */
	void setSavedImagePaths(List<String> paths);
	/** Returns the max number of images that can be saved on this storage item */
	int getMaxSaves();
	/** Save an image to this item */
	void saveImage(String path);
	/** Returns true if this storage item is not full */
	boolean canSave();
	
	@Override
	default NBTTagCompound serializeNBT() {
		NBTTagCompound ret = new NBTTagCompound();
		NBTTagList paths = new NBTTagList();
		for (int i = 0; i < getSavedImagePaths().length; i++) {
			String path = getSavedImagePaths()[i];
			NBTTagCompound pathNBT = new NBTTagCompound();
			pathNBT.setString("save" + i, path);
			paths.appendTag(pathNBT);
		}
		ret.setTag("Paths", paths);
		return ret;
	}

	@Override
	default void deserializeNBT(NBTTagCompound nbt) {
		NBTTagList paths = nbt.getTagList("Paths", 10);
		List<String> ret = new ArrayList<String>(getMaxSaves());
		for (int i = 0; i < paths.tagCount(); i++) {
			NBTTagCompound pathNBT = paths.getCompoundTagAt(i);
			ret.add(pathNBT.getString("save" + i));
		}
	}

	public static class SDCardHandler implements ICameraStorageNBT {

		private List<String> paths;
		
		public SDCardHandler() {
			paths = new ArrayList<String>(getMaxSaves());
		}

		@Override
		public String[] getSavedImagePaths() {
			return paths.toArray(new String[getMaxSaves()]);
		}
		
		@Override
		public void setSavedImagePaths(List<String> paths) {
			this.paths = paths;
		}

		@Override
		public int getMaxSaves() {
			return 32;
		}

		@Override
		public void saveImage(String path) {
			paths.add(path);
		}

		@Override
		public boolean canSave() {
			return paths.size() < getMaxSaves();
		}
	}
}
