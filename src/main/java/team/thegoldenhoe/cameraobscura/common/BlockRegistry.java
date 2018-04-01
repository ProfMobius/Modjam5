package team.thegoldenhoe.cameraobscura.common;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import team.thegoldenhoe.cameraobscura.common.craftstudio.BlockFake;
import team.thegoldenhoe.cameraobscura.common.craftstudio.BlockProps;

public class BlockRegistry {

    public static Block blockProps;
    public static Block blockFake;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();


        blockProps = registerBlock(registry, new BlockProps(), "blockProps");
        blockFake = registerBlock(registry, new BlockFake(), "blockFake");
    }

    private static Block registerBlock(IForgeRegistry<Block> registry, Block block, String name) {
        block.setRegistryName(name);
        registry.register(block);

        return block;
    }

}
