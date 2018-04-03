package team.thegoldenhoe.cameraobscura.common;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public interface ICameraNBT extends IItemHandler, INBTSerializable<NBTTagCompound> {
	
	ICameraStorageNBT getStorageDevice();
	Pair<IFilterNBT, IFilterNBT> getFilters();
	
    @Override
    default NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        System.out.println("serializing camera");
        return ret;
    }
    
    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
    	System.out.println("deserializing camera");
    }
    
    default void markDirty() {}
    
	public class CameraHandler extends ItemStackHandler implements ICameraNBT {
		public CameraHandler() {
			super(3);
		}
		
		@Override
		public void setSize(int size) {
			
		}
		
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			System.out.println("Contents changed");
			markDirty();
		}

		@Override
		public ICameraStorageNBT getStorageDevice() {
			ItemStack storageStack = getStackInSlot(0);
			
			if (!storageStack.isEmpty()) {
				if (storageStack.getItem() instanceof ItemSDCard) {
					return storageStack.getCapability(CameraCapabilities.getSDCardStorageCapability(), null);	
				} else if (storageStack.getItem() instanceof ItemPolaroidStack) {
					return storageStack.getCapability(CameraCapabilities.getPolaroidStackCapability(), null);
				} else if (storageStack.getItem() instanceof ItemVintagePaper) {
					return storageStack.getCapability(CameraCapabilities.getVintageStorageCapability(), null);
				}
			}
			
			return null;
		}

		@Override
		public Pair<IFilterNBT, IFilterNBT> getFilters() {
			// TODO
			return null;
		}

	}
}
