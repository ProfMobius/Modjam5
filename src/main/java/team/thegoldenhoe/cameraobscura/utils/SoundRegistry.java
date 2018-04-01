package team.thegoldenhoe.cameraobscura.utils;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.Info;

import java.util.Collection;
import java.util.HashMap;

public class SoundRegistry {

    private static HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<String, SoundEvent>();

    public static void register(String soundPath) {
        ResourceLocation resLoc = new ResourceLocation(Info.MODID, soundPath);
        SoundEvent event = new SoundEvent(resLoc);
        event.setRegistryName(resLoc);

        ForgeRegistries.SOUND_EVENTS.register(event);

        if (lookupStringToEvent.containsKey(soundPath)) {
            System.out.println("DECOCRAFT WARNING: duplicate sound registration for " + soundPath);
        }
        lookupStringToEvent.put(soundPath, event);
    }

    public static SoundEvent get(String soundPath) {
        return lookupStringToEvent.get(soundPath);
    }

    public static void registerAllSounds(Collection<CSModelMetadata> values) {
        lookupStringToEvent.clear();
        for (CSModelMetadata decoModelMetadata : values) {
            if (decoModelMetadata.sound != null) {
                register(decoModelMetadata.sound);
            }
        }
    }
}