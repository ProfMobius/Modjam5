package team.thegoldenhoe.cameraobscura.common.craftstudio;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

public class TileFake extends TileEntity {
	public int[] master = new int[3];

	public void setMasterTile(TileProps master) {
		this.master[0] = master.getPos().getX();
		this.master[1] = master.getPos().getY();
		this.master[2] = master.getPos().getZ();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return false;
	}

	public TileProps getMaster() {
		// Triggers a crash in some cases with Railcraft special air thingy
		// http://pastebin.com/gwvHir49
		// Fixed by checking the type is correct, and if not, we just invalidate the TE because whynot.

		TileEntity tile = ModelHandler.getTileEntityWithoutCreating(world, new BlockPos(master[0], master[1], master[2]));
		if (!(tile instanceof TileProps)){
			//this.worldObj.setBlockToAir(this.getPos());
			//this.invalidate();
			return null;
		}

		return (TileProps)tile;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.master = compound.getIntArray("master");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagIntArray masterTag = new NBTTagIntArray(master);
		compound.setTag("master", masterTag);
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.getPos(), 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}
}
