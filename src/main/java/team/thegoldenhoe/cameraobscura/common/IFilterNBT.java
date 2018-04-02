package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IFilterNBT extends INBTSerializable<NBTTagCompound> {

	@Override
	default NBTTagCompound serializeNBT() {
		NBTTagCompound ret = new NBTTagCompound();
		return ret;
	}

	@Override
	default void deserializeNBT(NBTTagCompound nbt) {

	}

	public static class FilterHandler implements IFilterNBT {

	}
}
