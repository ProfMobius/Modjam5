package team.thegoldenhoe.cameraobscura;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.thegoldenhoe.cameraobscura.common.item.ItemRegistry;

public enum TabProps {
    Main(0),;

    private PropTab tab;

    class PropTab extends CreativeTabs {
        private int meta;
        private ItemStack icon;

        public PropTab(int meta) {
            super("cultivation." + name());
            this.meta = meta;
        }

        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ItemRegistry.itemProps, 1);
        }


        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getIconItemStack() {
            if (this.icon == null) {
                this.icon = new ItemStack(ItemRegistry.itemProps, 1, this.meta);
            }

            return this.icon;
        }
    }

    TabProps(int meta) {
        this.tab = new PropTab(meta);
    }

    public PropTab get() {
        return this.tab;
    }

    public static CreativeTabs[] getAll() {
        CreativeTabs[] tabs = new CreativeTabs[TabProps.values().length + 1];
        tabs[0] = CreativeTabs.SEARCH;
        for (int i = 0; i < TabProps.values().length; i++) {
            tabs[i + 1] = TabProps.values()[i].get();
        }

        return tabs;
    }
}
