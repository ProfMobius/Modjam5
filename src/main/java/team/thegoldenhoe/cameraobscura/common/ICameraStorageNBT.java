package team.thegoldenhoe.cameraobscura.common;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICameraStorageNBT extends INBTSerializable<NBTTagCompound> {

	/** Returns paths to each of the images saved on this storage item */
	ArrayList<String> getSavedImagePaths();
	/** Override the current list of saved paths with a new one */
	void setSavedImagePaths(ArrayList<String> paths);
	/** Returns the max number of images that can be saved on this storage item */
	int getMaxSaves();
	/** Save an image to this item */
	void saveImage(String path);
	/** Returns true if this storage item is not full */
	boolean canSave();

	@Override
	default NBTTagCompound serializeNBT() {
		System.out.println("SERIALIZING NBT IN STORAGE DEVICE");
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
		System.out.println("NBT path count:" + paths.tagCount());
		return ret;
	}

	@Override
	default void deserializeNBT(NBTTagCompound nbt) {
		System.out.println("DESERIALIZING NBT IN STORAGE DEVICE");
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

	public static class PolaroidStackStorage implements ICameraStorageNBT {
		private ArrayList<String> paths;

		public PolaroidStackStorage() {
			paths = new ArrayList<String>(getMaxSaves());
		}

		@Override
		public ArrayList<String> getSavedImagePaths() {
			for (String path : paths) {
				System.out.println("Path: " + path);
			}
			return paths;
		}

		@Override
		public void setSavedImagePaths(ArrayList<String> paths) {
			this.paths = paths;
		}

		@Override
		public int getMaxSaves() {
			return 6;
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
		public void saveImage(String path) {
			paths.add(path);
		}

		@Override
		public boolean canSave() {
			return paths.size() < getMaxSaves();
		}
	}
}
