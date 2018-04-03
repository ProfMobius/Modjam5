package team.thegoldenhoe.cameraobscura.client;

import com.mia.craftstudio.minecraft.client.CSClientModelWrapperVBO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.client.renderers.RenderPropInv;
import team.thegoldenhoe.cameraobscura.common.craftstudio.TileProps;
import team.thegoldenhoe.cameraobscura.common.item.ItemProps;
import team.thegoldenhoe.cameraobscura.common.item.ItemRegistry;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;


public enum ClientEvents {
    INSTANCE;

    private int playerOrientation;
    private boolean canPlace;
    private CSModelMetadata model = null;
    private int tx, ty, tz;
    private int tick = 0;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SubscribeEvent
    public void capturePhotographs(RenderTickEvent event) {
        // Check if:
        // - A photograph is pending capture
        // - Gui is hidden
        // - It is the end phase of the render tick. If it's not the end phase, the gui
        //   will still be showing since the buffer hasn't been cleared yet
        if (ClientProxy.photographPending && Minecraft.getMinecraft().gameSettings.hideGUI
                && event.phase == TickEvent.Phase.END) {
            PhotographHelper.capturePhotograph();
            // Restore hide gui setting to whatever it was before
            Minecraft.getMinecraft().gameSettings.hideGUI = ClientProxy.hideGUIDefault;
            ClientProxy.photographPending = false;
        }
    }

    @SubscribeEvent
    public void onModelBake(final ModelBakeEvent event) {
        // TODO : Here we put the backing (based on ichun trailmix)
        // TODO : This is not valid anymore for 1.10
        event.getModelRegistry().putObject(new ModelResourceLocation("cameraobscura:csitem", "inventory"), new RenderPropInv());
    }

    @SubscribeEvent
    public void onItemTooltip(final ItemTooltipEvent event) {
        if (event.getItemStack().getItem().equals(ItemRegistry.itemProps)) {
            if (event.getItemStack().hasTagCompound() && event.getItemStack().getTagCompound().hasKey("savedProp")) {
                event.getToolTip().add(TextFormatting.RESET + "" + TextFormatting.AQUA + I18n.translateToLocal("text.cultivation.tooltip.saved_item"));
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (tick++ % 4 == 0) {
            model = null;

            final EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null) return;

            final ItemStack currentItem = player.inventory.getCurrentItem();
            if (currentItem == null) return;
            if (!(currentItem.getItem() instanceof ItemProps)) return;
            //if (!(Cultivation.data[currentItem.getItemDamage()] instanceof CraftStudioModelData)) return;

            if (!ModelHandler.getModelFromStack(currentItem).placeable) {
                return;
            }

            final RayTraceResult target = Minecraft.getMinecraft().objectMouseOver;
            if (target == null) return;
            if (target.typeOfHit != RayTraceResult.Type.BLOCK) return;

            final CSModelMetadata data = ModelHandler.getModelFromStack(currentItem);

            playerOrientation = 0;
            if (!data.limitRotation) {
                playerOrientation = MathHelper.floor(player.rotationYaw * 16.0F / 360.0F + 0.5D) & 15;
            } else {
                final int direction = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
                playerOrientation = direction * 4;
            }

            final EnumFacing fg = target.sideHit;
            tx = target.getBlockPos().getX() + fg.getFrontOffsetX();
            ty = target.getBlockPos().getY() + fg.getFrontOffsetY();
            tz = target.getBlockPos().getZ() + fg.getFrontOffsetZ();

            canPlace = TileProps.canPlace(data.wrapper, player.world, tx, ty, tz, player.isSneaking(), playerOrientation);
            model = data;
        }
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(final DrawBlockHighlightEvent event) {
        if (model != null && model.wrapper != null && model.showOutline) {
            ((CSClientModelWrapperVBO) model.wrapper).renderPlacement(event.getPlayer(), event.getPartialTicks(), canPlace, tx, ty, tz, playerOrientation);
        }
    }

}
