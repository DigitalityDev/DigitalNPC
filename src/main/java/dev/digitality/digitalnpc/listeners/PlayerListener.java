package dev.digitality.digitalnpc.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.NPC;

public class PlayerListener implements Listener {
    public PlayerListener() {
        Bukkit.getScheduler().runTaskTimer(DigitalNPC.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(this::handleMove);
        }, 0L, 2L);
    }

    private void handleMove(Player player) {
        for (NPC npc : DigitalNPC.getNpcList()) {
            if (!npc.getShown().contains(player)) {
                continue;
            }

            if (!npc.isShownTo(player) && npc.inRangeOf(player) && npc.inViewOf(player)) {
                npc.show(player, true);
            }

            if (npc.isShownTo(player) && !npc.inRangeOf(player)) {
                npc.hide(player, true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DigitalNPC.getNpcList().forEach(npc -> {
            npc.getAutoHidden().remove(e.getPlayer());
            npc.getShown().remove(e.getPlayer());
            npc.getRegisteredTeams().remove(e.getPlayer());

            if (npc.getPlayerInfoTimers().containsKey(e.getPlayer())) {
                npc.getPlayerInfoTimers().get(e.getPlayer()).cancel();
                npc.getPlayerInfoTimers().remove(e.getPlayer());
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        for (NPC npc : DigitalNPC.getNpcList()) {
            if (npc.isShownTo(player) && npc.getLocation() != null && npc.getLocation().getWorld().equals(player.getWorld())) {
                npc.hide(player, true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location respawn = event.getRespawnLocation();

        if (respawn.getWorld() != null && respawn.getWorld().equals(player.getWorld())) {
            Bukkit.getScheduler().runTaskLater(DigitalNPC.getPlugin(), () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (player.getLocation().equals(respawn)) {
                    handleMove(player);
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        handleMove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMove(event.getPlayer());
    }
}
