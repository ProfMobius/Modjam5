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
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.PolaroidStackStorage;
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.SDCardStorage;
import team.thegoldenhoe.cameraobscura.common.ICameraStorageNBT.VintageStorage;

public class CameraCapabilities {

	@CapabilityInject(ICameraNBT.class)
	@Nonnull
	private static Capability<ICameraNBT> cameraCapability;
	
	@CapabilityInject(SDCardStorage.class)
	@Nonnull
	private static Capability<SDCardStorage> sdCardStorageCapability;
	
	@CapabilityInject(PolaroidStackStorage.class)
	@Nonnull
	private static Capability<PolaroidStackStorage> polaroidStackStorageCapability;
	
	@CapabilityInject(VintageStorage.class)
	@Nonnull
	private static Capability<VintageStorage> vintageStorageCapability;
	
	@Nonnull
	public static Capability<ICameraNBT> getCameraCapability() {
		return cameraCapability;
	}
	
	@Nonnull
	public static Capability<SDCardStorage> getSDCardStorageCapability() {
		return sdCardStorageCapability;
	}
	
	@Nonnull
	public static Capability<PolaroidStackStorage> getPolaroidStackCapability() {
		return polaroidStackStorageCapability;
	}
	
	@Nonnull
	public static Capability<VintageStorage> getVintageStorageCapability() {
		return vintageStorageCapability;
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
        CapabilityManager.INSTANCE.register(SDCardStorage.class, new Capability.IStorage<SDCardStorage>() {

            @Override
            public NBTBase writeNBT(Capability<SDCardStorage> capability, SDCardStorage instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<SDCardStorage> capability, SDCardStorage instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
        }, SDCardStorage::new);
        
        // Polaroid Stack capability
        CapabilityManager.INSTANCE.register(PolaroidStackStorage.class, new Capability.IStorage<PolaroidStackStorage>() {

            @Override
            public NBTBase writeNBT(Capability<PolaroidStackStorage> capability, PolaroidStackStorage instance, EnumFacing side) {
            	System.out.println("Writing to polaroid nbt");
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<PolaroidStackStorage> capability, PolaroidStackStorage instance, EnumFacing side, NBTBase nbt) {
            	System.out.println("Reading from polaroid nbt");
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
        }, PolaroidStackStorage::new);
        
        // Vintage capability
        CapabilityManager.INSTANCE.register(VintageStorage.class, new Capability.IStorage<VintageStorage>() {

            @Override
            public NBTBase writeNBT(Capability<VintageStorage> capability, VintageStorage instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<VintageStorage> capability, VintageStorage instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
        }, VintageStorage::new);
        
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
