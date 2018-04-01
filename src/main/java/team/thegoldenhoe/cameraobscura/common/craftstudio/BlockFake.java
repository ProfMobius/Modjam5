package team.thegoldenhoe.cameraobscura.common.craftstudio;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFake extends BlockContainer implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockFake() {
        super(Material.CLOTH);
        this.setHardness(0.25f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileFake();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileFake) {
            TileProps master = ((TileFake) tile).getMaster();
            if (master != null) {
                return CameraObscura.blockProps.collisionRayTrace(blockState, world, master.getPos(), origin, direction);
            }
        }
        //System.out.printf("%d %d %d\n", tile.master[0], tile.master[1], tile.master[2] );
        return super.collisionRayTrace(blockState, world, pos, origin, direction);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileFake) {
            TileProps master = ((TileFake) tile).getMaster();
            if (master != null) {
                BlockPos masterPos = master.getPos();
                CameraObscura.blockProps.addCollisionBoxToList(world.getBlockState(masterPos), world, masterPos, entityBox, collidingBoxes, entityIn, p_185477_7_);
            }
        } else {
            //System.out.printf("%d %d %d\n", tile.master[0], tile.master[1], tile.master[2] );
            super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileFake) {
            TileProps master = ((TileFake) tile).getMaster();
            if (master != null) {
                BlockPos masterPos = master.getPos();
                return CameraObscura.blockProps.getCollisionBoundingBox(worldIn.getBlockState(masterPos), worldIn, masterPos);
            }
        }
        //System.out.printf("%d %d %d\n", tile.master[0], tile.master[1], tile.master[2] );
        return super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity collidingEntity) {
        TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileFake) {
            TileProps master = ((TileFake) tile).getMaster();
            if (master != null && master.getModelData().spiderweb) {
                BlockProps.onEntityWebbed(collidingEntity, world);
            }
        }
    }
}
