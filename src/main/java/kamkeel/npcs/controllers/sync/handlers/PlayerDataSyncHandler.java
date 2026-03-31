package kamkeel.npcs.controllers.sync.handlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumSyncAction;
import kamkeel.npcs.network.packets.data.large.SyncPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import kamkeel.npcs.controllers.sync.SyncHandler;
import kamkeel.npcs.network.enums.SyncType;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.data.PlayerData;

/**
 * Sync handler for {@link SyncType#PLAYERDATA}.
 * Special non-cached per-player path. Not included in login cache iteration.
 * RELOAD and UPDATE are sent as direct per-player packets.
 */
public class PlayerDataSyncHandler implements SyncHandler {

    private static final PlayerDataSyncHandler INSTANCE = new PlayerDataSyncHandler();

    public static PlayerDataSyncHandler getInstance() {
        return INSTANCE;
    }

    public void syncPlayerData(EntityPlayerMP player, boolean update) {
        PlayerData data = PlayerData.get(player);
        if (data != null) {
            if (update) {
                PacketHandler.Instance.sendToPlayer(new SyncPacket(SyncType.PLAYERDATA, EnumSyncAction.UPDATE, -1, data.getSyncNBT()), player);
            } else {
                PacketHandler.Instance.sendToPlayer(new SyncPacket(SyncType.PLAYERDATA, EnumSyncAction.RELOAD, -1, data.getSyncNBTFull()), player);
            }
        }
    }

    // ========== SERVER-SIDE ==========

    /**
     * PLAYERDATA is not a global cached type — it is per-player.
     */
    @Override
    public NBTTagCompound serializeAll() {
        return null;
    }

    // ========== CLIENT-SIDE ==========

    @SideOnly(Side.CLIENT)
    @Override
    public void clientHandleReload(NBTTagCompound fullCompound) {
        ClientCacheHandler.playerData.setSyncNBTFull(fullCompound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientHandleUpdate(NBTTagCompound compound, int categoryId) {
        ClientCacheHandler.playerData.setSyncNBT(compound);
    }

    @Override
    public boolean supportsUpdate() {
        return true;
    }

    /**
     * Not a cached type — PLAYERDATA uses direct per-player sync.
     */
    @Override
    public boolean isCachedType() {
        return false;
    }
}
