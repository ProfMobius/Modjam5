package team.thegoldenhoe.cameraobscura.common.capability;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import team.thegoldenhoe.cameraobscura.client.PhotoFilter;
import team.thegoldenhoe.cameraobscura.common.item.ItemFilter;
import team.thegoldenhoe.cameraobscura.common.item.ItemPolaroidStack;
import team.thegoldenhoe.cameraobscura.common.item.ItemSDCard;
import team.thegoldenhoe.cameraobscura.common.item.ItemVintagePaper;
import team.thegoldenhoe.cameraobscura.common.item.ItemFilter.FilterType;

public interface ICameraNBT extends IItemHandler, INBTSerializable<NBTTagCompound> {
	
	ICameraStorageNBT getStorageDevice();
	Pair<PhotoFilter, PhotoFilter> getFilters();
    
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
		
		public Pair<PhotoFilter, PhotoFilter> getFilters() {
			if (this.getSlots() == 1) {
				// TODO: either vintage or polaroid, choose filters here accordingly
//				ItemStack storageStack = getStackInSlot(0);
//				
//				if (!storageStack.isEmpty()) {
//					if (storageStack.getItem())
//				}
//				
				return Pair.of(null, null);
			}
			ItemStack filter1 = getStackInSlot(1);
			ItemStack filter2 = getStackInSlot(2);
			PhotoFilter pf1 = null, pf2 = null;
			
			if (!filter1.isEmpty()) {
				if (filter1.getItem() instanceof ItemFilter) {
					pf1 = FilterType.VALUES[filter1.getItemDamage()].getFilter();
				}
			}
			
			if (!filter2.isEmpty()) {
				if (filter2.getItem() instanceof ItemFilter) {
					pf2 = FilterType.VALUES[filter2.getItemDamage()].getFilter();
				}
			}
			
			return Pair.of(pf1, pf2);
		}

//		@Override
//		public Pair<IFilterNBT, IFilterNBT> getFilters() {
//			if (this.getSlots() < 3) return null;
//			ItemStack filter1 = getStackInSlot(1);
//			ItemStack filter2 = getStackInSlot(2);
//			
//			Pair<IFilterNBT, IFilterNBT> pair;
//			
//			if (!filter1.isEmpty()) {
//				IFilterNBT filter;
//			}
//		}

	}
}
