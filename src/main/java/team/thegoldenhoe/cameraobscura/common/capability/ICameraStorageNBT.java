package team.thegoldenhoe.cameraobscura.common.capability;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import team.thegoldenhoe.cameraobscura.common.item.ItemRegistry;

public interface ICameraStorageNBT extends INBTSerializable<NBTTagCompound> {

	/** Returns paths to each of the images saved on this storage item */
	ArrayList<String> getSavedImagePaths();
	/** Override the current list of saved paths with a new one */
	void setSavedImagePaths(ArrayList<String> paths);
	/** Returns the max number of images that can be saved on this storage item */
	int getMaxSaves();
	/** Save an image to this item */
	void saveImage(String path, EntityPlayer player);
	/** Returns true if this storage item is not full */
	boolean canSave();

	@Override
	default NBTTagCompound serializeNBT() {
		NBTTagCompound ret = new NBTTagCompound();
		NBTTagList paths = new NBTTagList();
		ArrayList<String> pathsList = getSavedImagePaths();
		for (int i = 0; i < pathsList.size(); i++) {
			String path = pathsList.get(i);
			if (path == null) continue;
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
		ArrayList<String> ret = new ArrayList<String>(getMaxSaves());
		for (int i = 0; i < paths.tagCount(); i++) {
			NBTTagCompound pathNBT = paths.getCompoundTagAt(i);
			ret.add(pathNBT.getString("save" + i));
		}
		this.setSavedImagePaths(ret);
	}

	public static class SDCardStorage implements ICameraStorageNBT {
		private ArrayList<String> paths;
		public static final int MAX_SAVES = 32;

		public SDCardStorage() {
			paths = new ArrayList<String>(getMaxSaves());
		}

		@Override
		public ArrayList<String> getSavedImagePaths() {
			return paths;
		}

		@Override
		public void setSavedImagePaths(ArrayList<String> paths) {
			this.paths = paths;
		}

		@Override
		public int getMaxSaves() {
			return MAX_SAVES;
		}

		@Override
		public void saveImage(String path, EntityPlayer player) {
			paths.add(path);
		}

		@Override
		public boolean canSave() {
			return paths.size() < getMaxSaves();
		}
	}

	public static class PolaroidStackStorage implements ICameraStorageNBT {
		private ArrayList<String> paths;
		public static final int MAX_SAVES = 6;

		public PolaroidStackStorage() {
			paths = new ArrayList<String>(getMaxSaves());
		}

		@Override
		public ArrayList<String> getSavedImagePaths() {
			return paths;
		}

		@Override
		public void setSavedImagePaths(ArrayList<String> paths) {
			this.paths = paths;
		}

		@Override
		public int getMaxSaves() {
			return MAX_SAVES;
		}

		@Override
		public void saveImage(String path, EntityPlayer player) {
			paths.add(path);
		}

		@Override
		public boolean canSave() {
			return paths.size() < getMaxSaves();
		}
	}

	public static class VintageStorage implements ICameraStorageNBT {
		private ArrayList<String> paths;

		public VintageStorage() {
			paths = new ArrayList<String>(getMaxSaves());
		}

		@Override
		public ArrayList<String> getSavedImagePaths() {
			return paths;
		}

		@Override
		public void setSavedImagePaths(ArrayList<String> paths) {
			this.paths = paths;
		}

		@Override
		public int getMaxSaves() {
			return 1;
		}

		@Override
		public void saveImage(String path, EntityPlayer player) {
			paths.add(path);

			ItemStack singlePhoto = new ItemStack(ItemRegistry.vintagePhoto);
			singlePhoto.setTagCompound(new NBTTagCompound());
			singlePhoto.getTagCompound().setString("Photo", path);

			player.addItemStackToInventory(singlePhoto);
		}

		@Override
		public boolean canSave() {
			return paths.size() < getMaxSaves();
		}
	}
}
