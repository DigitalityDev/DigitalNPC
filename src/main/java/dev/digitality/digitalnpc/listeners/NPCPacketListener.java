package dev.digitality.digitalnpc.listeners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.NPC;
import dev.digitality.digitalnpc.events.NPCInteractEvent;

public class NPCPacketListener extends SimplePacketListenerAbstract {
    private final Set<UUID> interactDelay = new HashSet<>();

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            Player player = event.getPlayer();
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

            if (interactDelay.contains(player.getUniqueId()))
                return;

            for (NPC npc : DigitalNPC.getNpcList()) {
                if (npc.getEntityId() == packet.getEntityId() && npc.getShown().contains(player)) {
                    NPCInteractEvent.ClickType clickType = packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK
                            ? NPCInteractEvent.ClickType.LEFT_CLICK
                            : NPCInteractEvent.ClickType.RIGHT_CLICK;

                    interactDelay.add(player.getUniqueId());
                    Bukkit.getScheduler().runTask(DigitalNPC.getPlugin(), () -> {
                        interactDelay.remove(player.getUniqueId());

                        if (!player.getWorld().equals(npc.getLocation().getWorld())) {
                            return;
                        }

                        double distance = player.getLocation().distanceSquared(npc.getLocation());
                        if (distance > 64) {
                            return;
                        }

                        Bukkit.getPluginManager().callEvent(new NPCInteractEvent(npc, player, clickType));
                    });
                }
            }
        }
    }
}
