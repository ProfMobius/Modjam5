package team.thegoldenhoe.cameraobscura.common.craftstudio;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mia.craftstudio.CSModel;
import com.mia.craftstudio.CSModel.ModelNode;
import com.mia.craftstudio.CSModel.ModelNode.Attrb;
import com.mia.craftstudio.libgdx.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.common.item.ItemRegistry;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;
import team.thegoldenhoe.cameraobscura.utils.SoundRegistry;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BlockProps extends BlockContainer implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockProps() {
        super(Material.CLOTH);
        this.setHardness(0.25f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
        ItemStack stack = null;
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);

        if (tile instanceof TileProps) {
            final Item item = ItemRegistry.itemProps;
            stack = item == null ? null : new ItemStack(item, 1, ((TileProps) tile).type);

            if (player.isSneaking()) {
                if (((TileProps) tile).isTileSavedOnDrop()) {
                    final NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
                    tag.removeTag("slaves");
                    stack.setTagInfo("savedProp", tag);
                }
            }
        }
        return stack;
    }

    @Override
    public void addCollisionBoxToList(final IBlockState state, final World world, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, @Nullable final Entity entityIn, final boolean p_185477_7_) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            final CSModelMetadata data = ModelHandler.getModelByID(((TileProps) tile).type);
            if (data.walkthrough) return;

            final CSModel model = data.csmodel;
            for (final ModelNode node : model.getNodes()) {
                if (!(node.hasAttribute(Attrb.PASSABLE) || node.hasAttribute(Attrb.PASSABLEPROPAGATES))) {        // This magic check ensure that blocks ending by _ are not added to the BB list, meaning they are passable.
                    final Vector3[] extend = data.wrapper.nodeCache.get(node).getExtend(((TileProps) tile).rotation);
                    final AxisAlignedBB aabb = new AxisAlignedBB(extend[0].x, extend[0].y, extend[0].z, extend[1].x, extend[1].y, extend[1].z);
                    final AxisAlignedBB aabbTmp = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
                    if (entityBox.intersects(aabbTmp)) {
                        collidingBoxes.add(aabbTmp);
                    }
                }
            }
        } else {
            super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    // XXX - MC1.9.4 : Defined to replace the now missing setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, BlockPos pos)
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(source, pos);
        if (tile instanceof TileProps) {
            final TileProps tileProps = (TileProps) tile;
            return ModelHandler.getModelByID(tileProps.type).getBoundingBox(tileProps.rotation);
        }
        return FULL_BLOCK_AABB;
    }

    // TODO: Note that these two methods are essentially the same code, the lower method was just a quick hack to return a maximum collision AABB. These should probably be combined to make life simpler in the future.
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileProps) {
            final CSModelMetadata data = ModelHandler.getModelByID(((TileProps) tile).type);
            if (data.walkthrough) return null;

            final CSModel model = data.csmodel;
            final AxisAlignedBB ret = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
            for (final ModelNode node : model.getNodes()) {
                if (!(node.hasAttribute(Attrb.PASSABLE) || node.hasAttribute(Attrb.PASSABLEPROPAGATES))) {        // This magic check ensure that blocks ending by _ are not added to the BB list, meaning they are passable.
                    final Vector3[] extend = data.wrapper.nodeCache.get(node).getExtend(((TileProps) tile).rotation);
                    final AxisAlignedBB aabb = new AxisAlignedBB(extend[0].x, extend[0].y, extend[0].z, extend[1].x, extend[1].y, extend[1].z);
                    final AxisAlignedBB aabbTmp = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
                    ret.union(aabbTmp);
                }
            }

            return ret;
        } else {
            return super.getCollisionBoundingBox(blockState, worldIn, pos);
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(final IBlockState state, final World world, final BlockPos pos, final Vec3d origin, final Vec3d direction) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            final CSModelMetadata data = ModelHandler.getModelByID(((TileProps) tile).type);

            RayTraceResult closest = null;
            final CSModel model = data.csmodel;
            for (final ModelNode node : model.getNodes()) {
                final Vector3[] extend = data.wrapper.nodeCache.get(node).getExtend(((TileProps) tile).rotation);
                final AxisAlignedBB aabb = new AxisAlignedBB(extend[0].x, extend[0].y, extend[0].z, extend[1].x, extend[1].y, extend[1].z);
                final RayTraceResult mop = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).calculateIntercept(origin, direction);

                if (mop != null) {
                    if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
                        closest = mop;
                    } else {
                        closest = mop;
                    }
                }
            }

            if (closest != null) {
                closest = new RayTraceResult(closest.hitVec, closest.sideHit, pos);
            }
            return closest;
        }

        return super.collisionRayTrace(state, world, pos, origin, direction);
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        try {
            return ModelHandler.getModelByID(meta).tileType.getTileClass().newInstance();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            System.out.printf("!!! CRITICAL ERROR : PRB WHILE LOADING TE FOR META %s !!!\n", meta);
            throw new RuntimeException(e);
        }
        return new TileProps();
    }

    @Override
    public ArrayList<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        final ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        return ret;
    }

    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            return ((TileProps) tile).getLightValue(world, pos);
        }
        return 0;
    }

    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            ((TileProps) tile).onBlockHarvested(world, pos, state, player);
        }
    }

    @Override
    public void onBlockClicked(final World world, final BlockPos pos, final EntityPlayer player) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            ((TileProps) tile).onBlockClicked(world, pos, player);
        }
    }

    private void playBlockSound(final World world, final TileProps prop, final Entity player) {
        if (world.isRemote)
            return;

        if (prop != null) {
            final String sfx = ModelHandler.getModelByID(prop.type).sound;
            if (sfx != null) {
                world.playSound((EntityPlayer) null, prop.getPos(), SoundRegistry.get(sfx), SoundCategory.AMBIENT, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos, final IBlockState state, final EntityPlayer playerIn, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileProps) {
            playBlockSound(worldIn, ((TileProps) tile), playerIn);
            return ((TileProps) tile).onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState stateIn, final World worldIn, final BlockPos pos, final Random rand) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileProps) {
            ((TileProps) tile).renderParticles(worldIn, pos, stateIn, rand);
        }
    }

    @Override
    public boolean isBed(final IBlockState state, final IBlockAccess world, final BlockPos pos, final Entity player) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            return ((TileProps) tile).isBed();
        }
        return false;
    }

    @Override
    public boolean isBedFoot(final IBlockAccess world, final BlockPos pos) {
        return super.isBedFoot(world, pos);
    }

    @Override
    public void setBedOccupied(final IBlockAccess world, final BlockPos pos, final EntityPlayer player, final boolean occupied) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            ((TileProps) tile).setBedOccupied(world, pos, player, occupied);
        }
    }

    @Override
    public BlockPos getBedSpawnPosition(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (tile instanceof TileProps) {
            return ((TileProps) tile).getBedSpawnPosition(world, pos, player);
        }
        return pos;
    }

    @Override
    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest) {
        final TileEntity master = ModelHandler.getTileEntityPreferNotCreating(world, pos);
        if (master instanceof TileProps) {
            ((TileProps) master).removeSlaves();
        }
        this.onBlockHarvested(world, pos, state, player);
        world.setBlockToAir(pos);
        return true;
    }

    // Small cache to prevent multiple repeated easter egg effects on the same entity until after they have not had an effect in awhile(and thus timed out of the cache)
    private static class TimedBool {
        public boolean value;
        public long time;

        public TimedBool(final boolean v, final long t) {
            value = v;
            time = t;
        }
    }

    private static final Cache<Object, TimedBool> eggedEntities = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(2500, TimeUnit.MILLISECONDS).build();

    private static void eggCacheAndSpawnEntity(final Entity newSpawn, final World world, final double x, final double y, final double z) {
        // Randomize the new entity's position slightly
        newSpawn.setLocationAndAngles(x + ((world.rand.nextFloat() - 0.5f) / 4f), y + ((world.rand.nextFloat() - 0.5f) / 4f), z + ((world.rand.nextFloat() - 0.5f) / 4f), world.rand.nextFloat() * 360f, 0f);
        newSpawn.setInWeb();
        world.spawnEntity(newSpawn);
        // Add to egged cache, but state it hasn't been egged yet; thus adding a noticeable delay in game for entity duplication
        eggedEntities.put(newSpawn, new TimedBool(false, System.currentTimeMillis()));
    }

    static void onEntityWebbed(final Entity webbedEntity, final World world) {
        webbedEntity.setInWeb(); // For whatever reason this has to be set on both the client and the server

        if (world.isRemote || webbedEntity.isDead)
            return;

        TimedBool egged = eggedEntities.getIfPresent(webbedEntity);

        if (egged == null || (!egged.value && ((egged.time + 500) <= System.currentTimeMillis()))) {
            // Trigger easter egg effects if:
            // - is not in cache (has not had it happen yet or recently)
            // - if it has not yet been egged and a half second has passed since it was first cached(delay prevents immediate recursion of spawned entities)
            if (egged == null) {
                egged = new TimedBool(true, System.currentTimeMillis());
            }
            egged.value = true;
            eggedEntities.put(webbedEntity, egged);
            if (webbedEntity instanceof EntitySpider) {
                if (world.rand.nextFloat() <= 0.3) {
                    // Reflect to get things that extend the base spider, such as cave spiders...
                    final Constructor[] ctors = webbedEntity.getClass().getDeclaredConstructors();
                    Constructor ctor = null;
                    for (int i = 0; i < ctors.length; i++) {
                        if (ctors[i].getParameterTypes().length == 1 && ctors[i].getParameterTypes()[0].isAssignableFrom(World.class)) {
                            ctor = ctors[i];
                            break;
                        }
                    }
                    if (ctor != null) {
                        try {
                            eggCacheAndSpawnEntity((Entity) ctor.newInstance(world), world, webbedEntity.posX, webbedEntity.posY, webbedEntity.posZ);
                        } catch (final InstantiationException e) {
                        } catch (final IllegalAccessException e) {
                        } catch (final IllegalArgumentException e) {
                        } catch (final InvocationTargetException e) {
                        } // Ignore all errors, if there is a problem spawning a cloned entity, then the cache entry will stop further attempts
                    }
                }
            } else if (webbedEntity instanceof EntityPlayer) {
                if (world.rand.nextFloat() <= 0.5) {
                    eggCacheAndSpawnEntity(new EntitySpider(world), world, webbedEntity.posX, webbedEntity.posY, webbedEntity.posZ);
                }
            }
        }
    }

    @Override
    public void onEntityCollidedWithBlock(final World worldIn, final BlockPos pos, final IBlockState state, final Entity entityIn) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileProps) {
            if (((TileProps) tile).getModelData().spiderweb) {
                onEntityWebbed(entityIn, worldIn);
            }
        }
    }

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileProps) {
            ((TileProps) tile).neighborChanged(state, worldIn, pos, blockIn, fromPos);
        }
    }

    @Override
    public boolean getTickRandomly() {
        return true;
    }

    @Override
    public void randomTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random random) {
        final TileEntity tile = ModelHandler.getTileEntityPreferNotCreating(worldIn, pos);
        if (tile instanceof TileProps) {
            ((TileProps) tile).randomTick(worldIn, pos, state, random);
        }
    }
}