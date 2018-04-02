package team.thegoldenhoe.cameraobscura.common;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public interface ICameraNBT extends IItemHandler, INBTSerializable<NBTTagCompound> {
	
	ICameraStorageNBT getSDCard();
	Pair<IFilterNBT, IFilterNBT> getFilters();
	
    @Override
    default NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        return ret;
    }
    
    @Override
    default void deserializeNBT(NBTTagCompound nbt) {

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
			markDirty();
		}

		@Override
		public ICameraStorageNBT getSDCard() {
			ItemStack sdCard = getStackInSlot(0);
			
			if (!sdCard.isEmpty()) {
				return sdCard.getCapability(CameraCapabilities.getSDCardCapability(), null);
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
