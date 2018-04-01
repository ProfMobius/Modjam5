package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandler;

public class ContainerCamera extends Container {

	protected final IItemHandler itemHandler;
	private IInventory playerInventory;
	
	public ContainerCamera(InventoryPlayer inventory, IItemHandler itemHandler, EnumHand hand) {
		this.itemHandler = itemHandler;
		this.playerInventory = inventory;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.playerInventory.isUsableByPlayer(playerIn);
	}

}
