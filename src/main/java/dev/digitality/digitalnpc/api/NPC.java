package dev.digitality.digitalnpc.api;

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.enums.NPCState;
import dev.digitality.digitalnpc.events.NPCHideEvent;
import dev.digitality.digitalnpc.events.NPCShowEvent;
import dev.digitality.digitalnpc.utils.MineSkinAPI;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NPC {
    private final int entityId = -DigitalNPC.getRandom().nextInt(Integer.MAX_VALUE);
    private final UUID uuid = new UUID(DigitalNPC.getRandom().nextLong(), 0);
    private String name = uuid.toString().replace("-", "").substring(0, 10);

    private final List<Player> shown = new ArrayList<>();
    private final List<Player> autoHidden = new ArrayList<>();
    private final List<Player> registeredTeams = new ArrayList<>();
    private final Map<Player, BukkitTask> playerInfoTimers = new HashMap<>();

    private Location location = null;
    private List<String> text = new ArrayList<>();
    private final Map<Player, Hologram> holograms = new HashMap<>();
    private Skin skin = null;
    private final List<NPCState> states = new ArrayList<>(List.of(NPCState.STANDING));
    private EntityPose pose = null;
    private final Map<EquipmentSlot, ItemStack> items = new EnumMap<>(EquipmentSlot.class);

    public NPC() {
        DigitalNPC.getNpcList().add(this);
    }

    public NPC(Location location) {
        this();
        this.location = location;
    }

    public boolean inRangeOf(Player player) {
        if (player == null) {
            return false;
        }

        if (!player.getWorld().equals(location.getWorld())) {
            return false;
        }

        double distanceSquared = player.getLocation().distanceSquared(location);
        double bukkitRange = Bukkit.getViewDistance() << 4;

        return distanceSquared <= (DigitalNPC.getAutoHideDistance() * DigitalNPC.getAutoHideDistance())
                && distanceSquared <= (bukkitRange * bukkitRange);
    }

    public boolean inViewOf(Player player) {
        Vector dir = location.toVector().subtract(player.getEyeLocation().toVector()).normalize();

        return dir.dot(player.getEyeLocation().getDirection()) >= Math.cos(Math.toRadians(60));
    }

    public boolean isShownTo(Player player) {
        return shown.contains(player) && !autoHidden.contains(player);
    }

    public void show() {
        for (Player p : Bukkit.getOnlinePlayers())
            this.show(p);
    }

    public void show(Player p) {
        this.show(p, false);
    }

    public void show(Player p, boolean auto) {
        NPCShowEvent event = new NPCShowEvent(this, p, auto);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        if (auto) {
            this._show(p);

            autoHidden.remove(p);
        } else {
            shown.add(p);

            if (inRangeOf(p) && inViewOf(p)) {
                this._show(p);
            } else {
                autoHidden.add(p);
            }
        }

        shown.add(p);
    }

    private void _show(Player p) {
        NPCPackets.sendTeamPacket(p, this);
        NPCPackets.sendPlayerInfoPacket(p, this);
        NPCPackets.sendSpawnEntityPacket(p, this);
        NPCPackets.sendEntityHeadRotationPacket(p, this);
        NPCPackets.sendEntityMetadataPacket(p, this);
        NPCPackets.sendEntityEquipmentPacket(p, this);

        if (holograms.containsKey(p)) {
            holograms.get(p).setShowPlayer(p);
        } else if (!text.isEmpty()) {
            setText(p, text);
        }
    }

    public void hide() {
        for (Player p : shown)
            this._hide(p);

        shown.clear();
    }

    public void hide(Player p) {
        this.hide(p, false);
    }

    public void hide(Player p, boolean auto) {
        NPCHideEvent event = new NPCHideEvent(this, p, auto);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        if (!shown.contains(p)) {
            return;
        }

        if (auto) {
            this._hide(p);

            autoHidden.add(p);
        } else {
            shown.remove(p);

            if (inRangeOf(p)) {
                this._hide(p);
            } else {
                autoHidden.remove(p);
            }
        }
    }

    private void _hide(Player p) {
        NPCPackets.sendEntityDestroyPacket(p, this);
        NPCPackets.sendPlayerInfoRemovePacket(p, this);

        if (holograms.containsKey(p)) {
            holograms.get(p).setHidePlayer(p);
        }
    }

    public void setLocation(Location newLocation) {
        Location from = this.location.clone();
        this.location = newLocation;

        for (Player p : shown)
            NPCPackets.sendTeleportPacket(p, this, from);

        holograms.forEach((player, hologram) -> {
            DHAPI.moveHologram(hologram, location.clone().add(0, 2.5, 0));
        });
    }

    public void lookAt(Location target) {
        Location from = this.location.clone();

        Vector dirBetweenLocations = target.toVector().subtract(from.toVector());
        location.setDirection(dirBetweenLocations);

        for (Player p : shown)
            NPCPackets.sendTeleportPacket(p, this, from);
    }

    public void setText(List<String> text) {
        holograms.forEach((player, hologram) -> {
            HologramPage page = DHAPI.getHologramPage(hologram, 0);

            if (page == null || this.text.size() != page.getLines().size()) {
                return;
            }

            for (int i = 0; i < this.text.size(); i++) {
                if (!this.text.get(i).equals(page.getLines().get(i).getText())) {
                    return;
                }
            }

            DHAPI.setHologramLines(hologram, text);
        });

        this.text = text;
    }

    public void setText(Player p, List<String> text) {
        this.text = text;

        if (holograms.containsKey(p)) {
            DHAPI.setHologramLines(holograms.get(p), text);
        }

        if (!text.isEmpty()) {
            Hologram hologram = DHAPI.createHologram("npc_hologram_" + entityId + "_" + p.getUniqueId(), location.clone().add(0, 2.5, 0));
            hologram.setDefaultVisibleState(false);
            hologram.setShowPlayer(p);
            DHAPI.setHologramLines(hologram, text);

            holograms.put(p, hologram);
        }
    }

    public void setSkin(String uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(DigitalNPC.getPlugin(), () -> {
            NPC.Skin skin = MineSkinAPI.fetchSkinFromUUID(uuid);

            Bukkit.getScheduler().runTask(DigitalNPC.getPlugin(), () -> setSkin(skin));
        });
    }

    public void setSkin(Skin skin) {
        this.skin = skin;

        this.hide();
        this.show();
    }

    public void toggleState(NPCState state) {
        if (states.contains(state))
            states.remove(state);
        else
            states.add(state);

        for (Player p : shown)
            NPCPackets.sendEntityMetadataPacket(p, this);
    }

    public EntityPose getPose() {
        if (pose == null) {
            return states.contains(NPCState.CROUCHED) ? EntityPose.CROUCHING : EntityPose.STANDING;
        }

        return pose;
    }

    public void setPose(EntityPose pose) {
        this.pose = pose;

        for (Player p : shown)
            NPCPackets.sendEntityMetadataPacket(p, this);
    }

    public void playAnimation(WrapperPlayServerEntityAnimation.EntityAnimationType animation) {
        for (Player p : shown)
            NPCPackets.sendAnimationPacket(p, this, animation);
    }

    public void setItem(EquipmentSlot slot, ItemStack item) {
        items.put(slot, item);

        updateEquipment();
    }

    public void updateEquipment() {
        for (Player p : shown)
            NPCPackets.sendEntityEquipmentPacket(p, this);
    }

    @Data
    public static class Skin {
        private final String value, signature;
    }
}
