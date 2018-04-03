package team.thegoldenhoe.cameraobscura;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.item.ItemStack;
import team.thegoldenhoe.cameraobscura.common.item.ItemProps;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

public class Utils {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    /**
     * Creates a unique PNG file in the given directory named by a timestamp.  Handles cases where the timestamp alone
     * is not enough to create a uniquely named file, though it still might suffer from an unlikely race condition where
     * the filename was unique when this method was called, but another process or thread created a file at the same
     * path immediately after this method returned.
     */
    public static File getTimestampedPNGFileForDirectory(File gameDirectory) {
        String s = DATE_FORMAT.format(new Date()).toString();
        int i = 1;

        while (true) {
            File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");

            if (!file1.exists()) {
                return file1;
            }

            ++i;
        }
    }
    
    public static boolean isCamera(ItemStack stack) {
    	CSModelMetadata data = ModelHandler.getModelFromStack(stack);
    	return !stack.isEmpty() && !data.placeable && (stack.getItem() instanceof ItemProps);
    }
}
