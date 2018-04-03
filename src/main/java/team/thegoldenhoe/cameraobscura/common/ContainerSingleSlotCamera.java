package team.thegoldenhoe.cameraobscura.common;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import team.thegoldenhoe.cameraobscura.common.network.CameraTypes;

public class ContainerSingleSlotCamera extends Container implements ICameraContainer {

	protected final IItemHandler itemHandler;
	protected IInventory playerInventory;
	protected final String bgName;

	public ContainerSingleSlotCamera(InventoryPlayer inventory, IItemHandler itemHandler, EnumHand hand, String bgName, CameraTypes type) {
		this.itemHandler = itemHandler;
		this.playerInventory = inventory;
		this.bgName = bgName;
		
		// Stacks slot
		this.addSlotToContainer(new SlotItemHandler(itemHandler, 0, 80, 53) {
			/**
			 * Check if the stack is allowed to be placed in this slot.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				// this is hacky, but...modjam!
				boolean initialReqs = super.isItemValid(stack) && !this.getHasStack();
				if (type == CameraTypes.POLAROID) {
					return initialReqs && stack.getItem() instanceof ItemPolaroidStack;
				} else if (type == CameraTypes.VINTAGE) {
					return initialReqs && stack.getItem() instanceof ItemVintagePaper;
				}
				
				return false;
			}
		});

		for (int i1 = 0; i1 < 3; ++i1)
		{
			for (int k1 = 0; k1 < 9; ++k1)
			{
				this.addSlotToContainer(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
			}
		}

		for (int j1 = 0; j1 < 9; ++j1)
		{
			this.addSlotToContainer(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.playerInventory.isUsableByPlayer(playerIn);
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	@Nullable
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = null;
		Slot slot = (Slot)this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < this.playerInventory.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.playerInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(0).isItemValid(itemstack1)) {
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				} else if (this.playerInventory.getSizeInventory() <= 1 || !this.mergeItemStack(itemstack1, 1, this.playerInventory.getSizeInventory(), false)) {
					return ItemStack.EMPTY;
				}
			}
			//			if (this.getSlot(0).isItemValid(itemstack1)) {
			//				if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
			//					System.out.println("Couldn't transfer stack in slot");
			//					return ItemStack.EMPTY;
			//				}
			//			} else {
			//				return ItemStack.EMPTY;
			//			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	@Override
	public String getContainerBackground() {
		return this.bgName;
	}
}
