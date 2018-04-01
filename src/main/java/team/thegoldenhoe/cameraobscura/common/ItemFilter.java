package team.thegoldenhoe.cameraobscura.common;

import java.util.Locale;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import team.thegoldenhoe.cameraobscura.client.PhotoFilter;
import team.thegoldenhoe.cameraobscura.client.PhotoFilters;

public class ItemFilter extends Item {

	public ItemFilter() {
		super();
		setHasSubtypes(true);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (FilterType filter : FilterType.VALUES) {
			subItems.add(new ItemStack(this, 1, filter.getMeta()));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
	    return "item.cameraobscura." + FilterType.VALUES[stack.getMetadata() % FilterType.VALUES.length].getName();
	}
	
	public static enum FilterType implements IStringSerializable {
		SEPIA(PhotoFilters.SEPIA);
	
		private PhotoFilter filter;
		
		public static FilterType[] VALUES = values();
		
		FilterType(PhotoFilter filter) {
			this.filter = filter;
		}
		
		public PhotoFilter getFilter() {
			return this.filter;
		}
		
	    public int getMeta() {
	        return ((Enum<?>) this).ordinal();
	    }

		@Override
		public String getName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
