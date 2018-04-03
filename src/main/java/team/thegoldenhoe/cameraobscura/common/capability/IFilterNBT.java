package team.thegoldenhoe.cameraobscura.common.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import team.thegoldenhoe.cameraobscura.client.PhotoFilter;
import team.thegoldenhoe.cameraobscura.client.PhotoFilters;

public interface IFilterNBT extends INBTSerializable<NBTTagCompound> {

	PhotoFilter getPhotoFilter();
	
	@Override
	default NBTTagCompound serializeNBT() {
		NBTTagCompound ret = new NBTTagCompound();
		return ret;
	}

	@Override
	default void deserializeNBT(NBTTagCompound nbt) {

	}

	public static class GloomyFilter implements IFilterNBT {
		@Override
		public PhotoFilter getPhotoFilter() {
			return PhotoFilters.BLACK_AND_WHITE;
		}
	}
	
	public static class HappyFilter implements IFilterNBT {
		@Override
		public PhotoFilter getPhotoFilter() {
			return PhotoFilters.BRIGHT_AND_HAPPY;
		}
	}
	
	public static class SepiaFilter implements IFilterNBT {
		@Override
		public PhotoFilter getPhotoFilter() {
			return PhotoFilters.SEPIA;
		}
	}
	
	public static class RetroFilter implements IFilterNBT {
		@Override
		public PhotoFilter getPhotoFilter() {
			return PhotoFilters.VINTAGE;
		}
	}
	
	public static class HighContrastFilter implements IFilterNBT {
		@Override
		public PhotoFilter getPhotoFilter() {
			return PhotoFilters.HIGH_CONTRAST;
		}
	}
}
