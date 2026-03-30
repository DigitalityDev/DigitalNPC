package dev.digitality.digitalnpc.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.NPC;

/**
 * @author Jitse Boonstra
 */
public class ChunkListener implements Listener {
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        for (NPC npc : DigitalNPC.getNpcList()) {
            if (npc.getLocation() == null || !isSameChunk(npc.getLocation(), chunk))
                continue;

            for (Player p : npc.getShown()) {
                if (npc.getAutoHidden().contains(p)) {
                    continue;
                }

                npc.hide(p, true);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        for (NPC npc : DigitalNPC.getNpcList()) {
            if (npc.getLocation() == null || !isSameChunk(npc.getLocation(), chunk))
                continue;

            for (Player p : npc.getShown()) {
                if (!npc.getAutoHidden().contains(p)) {
                    continue;
                }

                if (npc.inRangeOf(p)) {
                    npc.show(p, true);
                }
            }
        }
    }

    private static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    private static boolean isSameChunk(Location loc, Chunk chunk) {
        return getChunkCoordinate(loc.getBlockX()) == chunk.getX()
                && getChunkCoordinate(loc.getBlockZ()) == chunk.getZ();
    }
}
