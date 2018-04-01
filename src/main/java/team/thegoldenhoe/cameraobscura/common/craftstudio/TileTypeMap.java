package team.thegoldenhoe.cameraobscura.common.craftstudio;

import net.minecraftforge.fml.common.registry.GameRegistry;

public enum TileTypeMap {
    Props(TileProps.class),
    ;

    private Class<? extends TileProps> clazz;

    TileTypeMap(final Class<? extends TileProps> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends TileProps> getTileClass() {
        return clazz;
    }

    public static TileTypeMap getTileType(final Class<? extends TileProps> clazz) {
        for (final TileTypeMap type : TileTypeMap.values()) {
            if (type.clazz == clazz) return type;
        }
        return Props;
    }

    public static void register() {
        for (final TileTypeMap type : TileTypeMap.values()) {
            // TODO : We are registering with minecraft: because we managed to release at least one version for a short time with this specific
            // TODO : namespace and this specific world version. ie : We are fucked and we can't run datafixers anymore until 1.13 :/

            final String namespace_target = "cameraobscura:";
            final String targetname = namespace_target + type.clazz.getCanonicalName().toLowerCase();
            GameRegistry.registerTileEntity(type.clazz, targetname);
        }
        GameRegistry.registerTileEntity(TileFake.class, "cameraobscura:" + TileFake.class.getCanonicalName().toLowerCase());
    }
}
