package team.thegoldenhoe.cameraobscura.utils;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class JsonHelper {
    public static ItemStack stackFromString(final String string) {
        final String[] split = string.split(";");
        if (split.length != 3) {
            throw new IllegalArgumentException();
        }
        return new ItemStack(Item.getByNameOrId(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
    }

    public static List<IBlockState> blocksFromString(final String string) {
        final List<IBlockState> states = Lists.newArrayList();
        final String[] stateStrings = string.split(";");

        for (final String state : stateStrings) {
            final IBlockState block = Block.getBlockFromName(state).getDefaultState();
            states.add(block);
        }
        return states;
    }
}
