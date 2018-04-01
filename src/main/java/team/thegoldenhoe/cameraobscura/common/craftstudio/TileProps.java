package team.thegoldenhoe.cameraobscura.common.craftstudio;

import com.mia.craftstudio.CSModel;
import com.mia.craftstudio.libgdx.Vector3;
import com.mia.craftstudio.minecraft.BlockDimensionalPosition;
import com.mia.craftstudio.minecraft.CraftStudioModelWrapper;
import com.mia.craftstudio.minecraft.IAnimatedTile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.common.ItemRegistry;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

import java.util.*;

public class TileProps extends TileEntity implements IAnimatedTile {

    public int oldLight = -1;
    public int type = 0;
    public int rotation;
    protected boolean isRunning = false;
    public Random rand = new Random();
    public Set<BlockPos> slaves = new HashSet<BlockPos>();
    public Map<String, String> tileParams = new HashMap<String, String>();
    protected boolean initialized = false;

    // This method is called on readFromNBT and on item usage after the TE is properly set in the world
    public void init() {
    }

    @Override
    public boolean shouldRenderInPass(final int pass) {
        // Could also be return true, but I am not sure if there can't be more passes
        return pass == 0 || pass == 1;
    }

    private BlockDimensionalPosition dimpos = null;

    @Override
    public BlockDimensionalPosition getBlockPosDim() {
        if (this.dimpos == null) {
            this.dimpos = new BlockDimensionalPosition(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
        }
        return this.dimpos;
    }

    ;

    // now make sure the cached position is cleared by some common update methods called when major block changes occur
    @Override
    public void invalidate() {
        super.invalidate();
        this.dimpos = null;
    }

    @Override
    public void validate() {
        super.validate();
        this.dimpos = null;
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        this.dimpos = null;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        type = compound.getInteger("BlockType");
        rotation = compound.getInteger("BlockRotation");
        isRunning = compound.hasKey("isRunning") ? compound.getBoolean("isRunning") : false;

        final NBTTagList slaves = compound.getTagList("slaves", 11);
        for (int i = 0; i < slaves.tagCount(); i++) {
            final int[] pos = slaves.getIntArrayAt(i);
            this.slaves.add(new BlockPos(pos[0], pos[1], pos[2]));
        }

        this.init();

        tileParams = ModelHandler.getModelByID(type).tileParams;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("BlockType", this.type);
        compound.setInteger("BlockRotation", this.rotation);
        compound.setBoolean("isRunning", this.isRunning);

        final NBTTagList slavesLst = new NBTTagList();
        for (final BlockPos slave : this.slaves) {
            final NBTTagIntArray pos = new NBTTagIntArray(new int[]{slave.getX(), slave.getY(), slave.getZ()});
            slavesLst.appendTag(pos);
        }
        compound.setTag("slaves", slavesLst);

        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.getPos(), 0, getUpdateTag());
    }

    private TileProps processTileData(final NBTTagCompound compound) {
        TileProps tile = this;
        if (compound.hasKey("BlockType") && !ModelHandler.getModelByID(compound.getInteger("BlockType")).tileType.getTileClass().equals(tile.getClass())) {
            tile.invalidate();
            tile = ((TileProps) ((BlockProps) CameraObscura.blockProps).createNewTileEntity(world, compound.getInteger("BlockType")));
            final Chunk chunk = world.getChunkFromBlockCoords(this.getPos()); //Forge add NPE protection
            if (chunk != null) chunk.addTileEntity(this.getPos(), tile);
            world.addTileEntity(tile);
        }

        tile.readFromNBT(compound);

        return tile;
    }

    private void processClientTile(final TileProps tile) {
        final BlockPos blockPos = tile.getPos();
        final IBlockState blockState = tile.world.getBlockState(blockPos);
        tile.world.notifyBlockUpdate(pos, blockState, blockState, 2);

        final int lightValue = tile.getLightValue(tile.world, blockPos);
        if (tile.world != null && tile.oldLight != lightValue) {
            tile.world.checkLight(blockPos);
            tile.oldLight = lightValue;
        }
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        final TileProps tile = processTileData(pkt.getNbtCompound());

        if (net.getDirection() == EnumPacketDirection.CLIENTBOUND) {
            processClientTile(tile);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(final NBTTagCompound tag) {
        processClientTile(processTileData(tag));
    }

    public void renderParticles(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
    }

    //	@Override
    //	public boolean canUpdate() {
    //		return false;
    //	}

    public void onBlockClicked(final World world, final BlockPos pos, final EntityPlayer player) {
    }

    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return false;
    }

    public int getMetadata() {
        return this.rotation;
    }

    public boolean getIsRunning() {
        return this.isRunning;
    }

    public boolean isBed() {
        return false;
    }

    public int getLightValue(final IBlockAccess world, final BlockPos pos) {
        if (this.getModelData() != null)
            return this.getModelData().lightLevel;
        else
            return 0;
    }

    public int getSizeInventoryX() {
        return this.getModelData().invX;
    }

    public int getSizeInventoryY() {
        return this.getModelData().invY;
    }

    public void onBlockHarvested(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player) {
        NBTTagCompound savedNBT = null;
        if (isTileSavedOnDrop()) {
            savedNBT = new NBTTagCompound();
            writeToNBT(savedNBT);
            savedNBT.removeTag("slaves");
        }

        if (!player.capabilities.isCreativeMode) {
            final ItemStack droppedItem = new ItemStack(ItemRegistry.itemProps, 1, type);
            if (savedNBT != null) {
                droppedItem.setTagInfo("savedProp", savedNBT);
            }
            Block.spawnAsEntity(world, pos, droppedItem);
        }
    }

    public boolean isTileSavedOnDrop() {
        return false;
    }

    public void addSlave(final TileFake slave) {
        this.slaves.add(slave.getPos());
    }

    public void rmSlave(final TileFake slave) {
        this.slaves.remove(slave.getPos());
        this.world.setBlockToAir(slave.getPos());
    }

    public void removeSlaves() {
        for (final BlockPos slave : this.slaves) {
            this.world.setBlockToAir(slave);
        }
    }

    public CSModelMetadata getModelData() {
        return ModelHandler.getModelByID(this.type);
    }

    public void markRenderDirty() {
    }

    public boolean sameID(final TileEntity tile) {
        if (!(tile instanceof TileProps))
            return false;

        return this.getModelData().decocraftModelID == ((TileProps) tile).getModelData().decocraftModelID;
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newSate) {
        //return true;
        return oldState.getBlock() != newSate.getBlock();
        //return !(world.getTileEntity(pos) instanceof TileProps);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        final Vector3[] bounds = ModelHandler.getModelByID(type).wrapper.getExtend(rotation);
        final BlockPos pos = this.getPos();
        return new AxisAlignedBB(pos.getX() + bounds[0].x, pos.getY() + bounds[0].y, pos.getZ() + bounds[0].z, pos.getX() + bounds[1].x, pos.getY() + bounds[1].y, pos.getZ() + bounds[1].z);
    }

    public void setBedOccupied(final IBlockAccess world, final BlockPos pos, final EntityPlayer player, final boolean occupied) {
    }

    public BlockPos getBedSpawnPosition(final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        return pos;
    }

    public int getRenderingType() {
        return type;
    }

    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
    }

    public void blockPlaced(final IBlockState state, final World worldIn, final BlockPos pos) {
    }

    public void randomTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random random) {
    }

    public void createSlaves() {
        // Looks like this part is triggering a prb when the block is air but already has a TE (Railcraft heat thingy comes to mind here).
        // Crashlog pending
        final CSModelMetadata data = ModelHandler.getModelByID(type);
        final int[] extendBlock = data.wrapper.getExtendPlacementBlock(rotation);

        for (int lx = extendBlock[0]; lx <= extendBlock[3]; lx++) {
            for (int ly = (data.csmodel.getRootNode() != null && data.csmodel.getRootNode().hasAttribute(CSModel.ModelNode.Attrb.IGNOREBELOWYPLANE) ? 0 : extendBlock[1]); ly <= extendBlock[4]; ly++) {
                for (int lz = extendBlock[2]; lz <= extendBlock[5]; lz++) {
                    if (!(lx == 0 && ly == 0 && lz == 0)) {
                        final BlockPos trgBlockPos = new BlockPos(lx + pos.getX(), ly + pos.getY(), lz + pos.getZ());
                        final Block targetBlock = world.getBlockState(trgBlockPos).getBlock();
                        if (targetBlock == CameraObscura.blockFake) {
                            final TileFake targetFake = (TileFake) (world.getTileEntity(trgBlockPos));
                            final TileProps targetMaster = targetFake.getMaster();
                            if (targetMaster != null) {
                                targetMaster.rmSlave(targetFake);
                            }
                        }

                        if (targetBlock != CameraObscura.blockProps) {
                            world.setBlockState(trgBlockPos, CameraObscura.blockFake.getDefaultState().withProperty(BlockFake.FACING, EnumFacing.NORTH));
                            final TileFake fakeTile = (TileFake) (world.getTileEntity(trgBlockPos));
                            fakeTile.setMasterTile(this);
                            this.addSlave(fakeTile);
                            fakeTile.markDirty();
                        }
                    }
                }
            }
        }
    }

    public static boolean canReplace(final CraftStudioModelWrapper model, final World world, final int x, final int y, final int z, final boolean isSneaking, final int orient, final int[] excludedArea) {
        final int[] extendBlock = model.getExtendPlacementBlock(orient);

        for (int lx = extendBlock[0]; lx <= extendBlock[3]; lx++) {
            for (int ly = (model.getMetadata().csmodel.getRootNode() != null && model.getMetadata().csmodel.getRootNode().hasAttribute(CSModel.ModelNode.Attrb.IGNOREBELOWYPLANE) ? 0 : extendBlock[1]); ly <= extendBlock[4]; ly++) {
                for (int lz = extendBlock[2]; lz <= extendBlock[5]; lz++) {

                    if (lx >= excludedArea[0] && lx <= excludedArea[3] && ly >= excludedArea[1] && ly <= excludedArea[4] && lz >= excludedArea[2] && lz <= excludedArea[5]) {
                        continue;
                    }

                    final IBlockState blockState = world.getBlockState(new BlockPos(lx + x, ly + y, lz + z));
                    final Block targetBlock = blockState.getBlock();
                    if (!((blockState.getMaterial() == Material.AIR)
                            || (targetBlock == Blocks.TALLGRASS)
                            || (targetBlock == Blocks.SNOW_LAYER)
                            || (isSneaking && targetBlock == CameraObscura.blockFake)
                            || (isSneaking && targetBlock == CameraObscura.blockProps)
                    ))
                        return false;
                }
            }
        }
        return true;
    }

    public static boolean canPlace(final CraftStudioModelWrapper model, final World world, final int x, final int y, final int z, final boolean isSneaking, final int orient) {
        final int[] extendBlock = model.getExtendPlacementBlock(orient);

        for (int lx = extendBlock[0]; lx <= extendBlock[3]; lx++) {
            for (int ly = (model.getMetadata().csmodel.getRootNode() != null && model.getMetadata().csmodel.getRootNode().hasAttribute(CSModel.ModelNode.Attrb.IGNOREBELOWYPLANE) ? 0 : extendBlock[1]); ly <= extendBlock[4]; ly++) {
                for (int lz = extendBlock[2]; lz <= extendBlock[5]; lz++) {
                    final IBlockState blockState = world.getBlockState(new BlockPos(lx + x, ly + y, lz + z));
                    final Block targetBlock = blockState.getBlock();
                    if (!((blockState.getMaterial() == Material.AIR)
                            || (targetBlock == Blocks.TALLGRASS)
                            || (targetBlock == Blocks.SNOW_LAYER)
                            || (isSneaking && targetBlock == CameraObscura.blockFake)
                            || (isSneaking && targetBlock == CameraObscura.blockProps)
                    ))
                        return false;
                }
            }
        }
        return true;
    }

    public static TileProps checkAndGetTileEntity(final World world, final BlockPos finalPos, final int type) {
        final TileEntity tileentity = world.getTileEntity(finalPos);
        if (!(tileentity instanceof TileProps)) {
            throw new RuntimeException(String.format("Something went terribly wrong. Invalid TE detected after placement ! - %s", tileentity));
        }
        // Check that the world has the correct TE type created
        TileProps tileProps = ((TileProps) ((BlockProps) CameraObscura.blockProps).createNewTileEntity(world, type));
        if (!tileProps.getClass().equals(tileentity.getClass())) {
            tileentity.invalidate();
            final Chunk chunk = world.getChunkFromBlockCoords(finalPos); //Forge add NPE protection
            if (chunk != null) chunk.addTileEntity(finalPos, tileProps);
            world.addTileEntity(tileProps);
        } else {
            tileProps = (TileProps) tileentity;
        }
        return tileProps;
    }

}
