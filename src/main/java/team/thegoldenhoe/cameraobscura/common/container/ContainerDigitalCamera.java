package team.thegoldenhoe.cameraobscura.common.container;

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
import team.thegoldenhoe.cameraobscura.common.capability.CameraCapabilities;
import team.thegoldenhoe.cameraobscura.common.capability.ICameraNBT;
import team.thegoldenhoe.cameraobscura.common.item.ItemFilter;
import team.thegoldenhoe.cameraobscura.common.item.ItemSDCard;

public class ContainerDigitalCamera extends Container implements ICameraContainer {

	protected final IItemHandler itemHandler;
	private IInventory playerInventory;

	public ContainerDigitalCamera(InventoryPlayer inventory, IItemHandler itemHandler, EnumHand hand) {
		this.itemHandler = itemHandler;
		this.playerInventory = inventory;

		// SD Card Slot
		this.addSlotToContainer(new SlotItemHandler(itemHandler, 0, 41, 53) {
			/**
			 * Check if the stack is allowed to be placed in this slot.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem() instanceof ItemSDCard && !this.getHasStack();
			}
		});
		// Filter Slot 1
		this.addSlotToContainer(new SlotItemHandler(itemHandler, 1, 93, 53) {
			/**
			 * Check if the stack is allowed to be placed in this slot.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem() instanceof ItemFilter && !this.getHasStack();
			}
		});

		// Filter Slot 2
		this.addSlotToContainer(new SlotItemHandler(itemHandler, 2, 119, 53) {
			/**
			 * Check if the stack is allowed to be placed in this slot.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem() instanceof ItemFilter && !this.getHasStack();
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
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            
            // TODO - shift click
            
            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

	@Override
	public String getContainerBackground() {
		return "camera_digital";
	}
}
