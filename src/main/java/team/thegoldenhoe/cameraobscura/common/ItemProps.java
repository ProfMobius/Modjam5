package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.CameraObscura;
import team.thegoldenhoe.cameraobscura.TabProps;
import team.thegoldenhoe.cameraobscura.common.craftstudio.BlockProps;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileProps;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

import java.util.ArrayList;
import java.util.Collections;

public class ItemProps extends Item {
    public ItemProps() {
        super();
        setMaxDamage(0);
        setHasSubtypes(true);
        setFull3D();
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = player.getHeldItem(hand);

        if (stack.getCount() == 0) {
            return EnumActionResult.FAIL;
        }

        CSModelMetadata data = ModelHandler.getModelFromStack(stack);
        if (data.placeable) {
            return place(player, world, pos, hand, side, hitX, hitY, hitZ);
        }

        return EnumActionResult.FAIL;
    }

    public EnumActionResult place(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = player.getHeldItem(hand);

        if (stack.getCount() == 0) {
            return EnumActionResult.FAIL;
        }

        CSModelMetadata data = ModelHandler.getModelFromStack(stack);

        final Block target = world.getBlockState(pos).getBlock();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if ((target != Blocks.TALLGRASS) && (target != Blocks.SNOW_LAYER)) {
            x += side.getFrontOffsetX();
            y += side.getFrontOffsetY();
            z += side.getFrontOffsetZ();
        }

        final BlockPos finalPos = new BlockPos(x, y, z);

        if (!player.canPlayerEdit(pos, side, stack)) {
            return EnumActionResult.FAIL;
        }

        if (data.placedModel != -1) {
            data = ModelHandler.getModelByID(data.placedModel);
        }

        if (!data.isBlockInList(world.getBlockState(finalPos.down()), "canPlaceOn")) {
            return EnumActionResult.FAIL;
        }

        int i1 = 0;
        if (!data.limitRotation) {
            i1 = MathHelper.floor(player.rotationYaw * 16.0F / 360.0F + 0.5D) & 15;
        } else {
            final int direction = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            i1 = direction * 4;
        }

        // We check the BB and target blocks to see if we are overlapping
        if (!TileProps.canPlace(data.wrapper, world, x, y, z, player.isSneaking(), i1)) {
            if (world.isRemote) {
                final ITextComponent error = new TextComponentString("You can't place that here. Something is in the way.");
                error.getStyle().setItalic(true);
                error.getStyle().setColor(TextFormatting.GRAY);
                player.sendMessage(error);
            }
            return EnumActionResult.FAIL;
        }

        // Moved from top of method to here so that the placement tests can at least be run client side before exiting
        if (world.isRemote) {
            return EnumActionResult.FAIL;
        }

        // ==== SETTING UP THE BLOCK AND TILEENTITY ====
        final Class tileClass = data.tileType.getTileClass();

        final int orient = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        final EnumFacing enumfacing = EnumFacing.getHorizontal(orient);

        if (!world.setBlockState(finalPos, CameraObscura.blockProps.getDefaultState().withProperty(BlockProps.FACING, enumfacing))) {
            return EnumActionResult.FAIL;
        }

        final TileProps tileProps = TileProps.checkAndGetTileEntity(world, finalPos, data.decocraftModelID);

        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("savedProp")) {
            tileProps.readFromNBT(stack.getTagCompound().getCompoundTag("savedProp"));
            tileProps.setPos(finalPos);
        }

        tileProps.type = data.decocraftModelID;
        tileProps.rotation = i1;
        tileProps.tileParams = data.tileParams;

        tileProps.init();
        tileProps.blockPlaced(world.getBlockState(finalPos), world, finalPos);
        tileProps.createSlaves();

        tileProps.markDirty();
        //world.notifyBlockUpdate(finalPos, world.getBlockState(finalPos), world.getBlockState(finalPos), 3);
        world.checkLight(finalPos);

        final SoundType soundtype = CameraObscura.blockProps.getSoundType();
        world.playSound((EntityPlayer) null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        stack.setCount(stack.getCount() - 1);

        player.stopActiveHand();
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        final ArrayList<CSModelMetadata> metaList = new ArrayList<CSModelMetadata>(ModelHandler.getAllModelMetadata());
        Collections.sort(metaList);
        for (final CSModelMetadata data : metaList) {
            if (tab == data.tab.get() || tab == CreativeTabs.SEARCH || tab == null) {
                subItems.add(new ItemStack(ItemRegistry.itemProps, 1, data.decocraftModelID));
            }
        }
    }

    @Override
    public int getMetadata(final int meta) {
        return meta;
    }

    @Override
    public String getUnlocalizedName() {
        return "item.cameraobscura.stuffed_creeper";
    }

    @Override
    public String getUnlocalizedName(final ItemStack stack) {
        return "item.cameraobscura." + ModelHandler.getModelFromStack(stack).name.toLowerCase().replace(" ", "_").replace("'", "_");
    }

    @Override
    public CreativeTabs[] getCreativeTabs() {
        return TabProps.getAll();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.SEARCH;
    }
}
