package team.thegoldenhoe.cameraobscura.common;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import team.thegoldenhoe.cameraobscura.common.ICameraNBT.CameraHandler;
import team.thegoldenhoe.cameraobscura.common.ISDCardNBT.SDCardHandler;

public class CameraCapabilities {

	@CapabilityInject(ICameraNBT.class)
	@Nonnull
	private static Capability<ICameraNBT> cameraCapability;
	
	@CapabilityInject(ISDCardNBT.class)
	@Nonnull
	private static Capability<ISDCardNBT> sdCardCapability;
	
	@Nonnull
	public static Capability<ICameraNBT> getCameraCapability() {
		return cameraCapability;
	}
	
	@Nonnull
	public static Capability<ISDCardNBT> getSDCardCapability() {
		return sdCardCapability;
	}
	
	public static void register() {
		// Camera capability
        CapabilityManager.INSTANCE.register(ICameraNBT.class, new Capability.IStorage<ICameraNBT>() {

            @Override
            public NBTBase writeNBT(Capability<ICameraNBT> capability, ICameraNBT instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ICameraNBT> capability, ICameraNBT instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
        }, CameraHandler::new);
        
        // SD Card capability
        CapabilityManager.INSTANCE.register(ISDCardNBT.class, new Capability.IStorage<ISDCardNBT>() {

            @Override
            public NBTBase writeNBT(Capability<ISDCardNBT> capability, ISDCardNBT instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ISDCardNBT> capability, ISDCardNBT instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
        }, SDCardHandler::new);
        
        // Filter capability
//        CapabilityManager.INSTANCE.register(ISDCardNBT.class, new Capability.IStorage<ISDCardNBT>() {
//
//            @Override
//            public NBTBase writeNBT(Capability<ISDCardNBT> capability, ISDCardNBT instance, EnumFacing side) {
//                return instance.serializeNBT();
//            }
//
//            @Override
//            public void readNBT(Capability<ISDCardNBT> capability, ISDCardNBT instance, EnumFacing side, NBTBase nbt) {
//                instance.deserializeNBT((NBTTagCompound) nbt);
//            }
//            
//        }, SDCardHandler::new);        
	}
	
    public static <C> ICapabilityProvider getProvider(final Capability<C> cap, final Supplier<C> factory) {
        return new ICapabilityProvider() {

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == cap;
            }

            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                return hasCapability(capability, facing) ? cap.cast(factory.get()) : null;
            }
        };
    }
}
