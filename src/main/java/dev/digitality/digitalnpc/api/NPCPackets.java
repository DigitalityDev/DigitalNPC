package dev.digitality.digitalnpc.api;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.enums.NPCState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

public class NPCPackets {
    public static void sendTeamPacket(Player player, NPC npc) {
        if (npc.getRegisteredTeams().contains(player))
            return;

        WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.text(npc.getName()),
                null,
                null,
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                NamedTextColor.WHITE,
                WrapperPlayServerTeams.OptionData.NONE
        );
        WrapperPlayServerTeams teamCreate = new WrapperPlayServerTeams(npc.getName(), WrapperPlayServerTeams.TeamMode.CREATE, teamInfo);

        WrapperPlayServerTeams teamJoin = new WrapperPlayServerTeams(npc.getName(), WrapperPlayServerTeams.TeamMode.ADD_ENTITIES, teamInfo, Collections.singletonList(npc.getName()));

        DigitalNPC.sendPacket(player, teamCreate);
        DigitalNPC.sendPacket(player, teamJoin);

        npc.getRegisteredTeams().add(player);
    }

    public static void sendPlayerInfoPacket(Player player, NPC npc) {
        UserProfile profile = new UserProfile(
                npc.getUuid(),
                npc.getName(),
                npc.getSkin() == null ? new ArrayList<>() : List.of(
                        new TextureProperty(
                                "textures",
                                npc.getSkin().getValue(),
                                npc.getSkin().getSignature()
                        )
                )
        );

        if (DigitalNPC.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            WrapperPlayServerPlayerInfoUpdate playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, Collections.singletonList(
                    new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                            profile,
                            false,
                            1,
                            GameMode.SURVIVAL,
                            Component.text(ChatColor.GRAY + "[NPC] " + npc.getName()),
                            null
                    )
            ));

            DigitalNPC.sendPacket(player, playerInfo);
        } else {
            WrapperPlayServerPlayerInfo playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, Collections.singletonList(
                    new WrapperPlayServerPlayerInfo.PlayerData(
                            Component.text(ChatColor.GRAY + "[NPC] " + npc.getName()),
                            profile,
                            GameMode.SURVIVAL,
                            1
                    )
            ));

            DigitalNPC.sendPacket(player, playerInfo);
        }

        if (npc.getPlayerInfoTimers().containsKey(player))
            npc.getPlayerInfoTimers().get(player).cancel();

        BukkitTask task = Bukkit.getScheduler().runTaskLater(DigitalNPC.getPlugin(), () -> {
            if (npc.getShown().contains(player)) {
                sendPlayerInfoRemovePacket(player, npc);
            }
        }, 200L);
        npc.getPlayerInfoTimers().put(player, task);
    }

    public static void sendSpawnEntityPacket(Player player, NPC npc) {
        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(
                npc.getEntityId(),
                npc.getUuid(),
                EntityTypes.PLAYER,
                SpigotConversionUtil.fromBukkitLocation(npc.getLocation()),
                0.0f,
                0,
                new Vector3d()
        );

        DigitalNPC.sendPacket(player, spawnEntity);
    }

    public static void sendEntityHeadRotationPacket(Player player, NPC npc) {
        WrapperPlayServerEntityHeadLook headRotationPacket = new WrapperPlayServerEntityHeadLook(
                npc.getEntityId(),
                npc.getLocation().getYaw()
        );

        DigitalNPC.sendPacket(player, headRotationPacket);
    }

    public static void sendEntityMetadataPacket(Player player, NPC npc) {
        WrapperPlayServerEntityMetadata entityMetadata = new WrapperPlayServerEntityMetadata(
                npc.getEntityId(),
                version -> {
                    List<EntityData<?>> data = new ArrayList<>();
                    data.add(new EntityData<>(0, EntityDataTypes.BYTE, NPCState.getMasked(npc.getStates()))); // Entity flags (crouching, sprinting, etc.)
                    data.add(new EntityData<>(6, EntityDataTypes.ENTITY_POSE, npc.getPose()));
                    data.add(new EntityData<>(version.isNewerThanOrEquals(ClientVersion.V_1_21_9) ? 16 : 17, EntityDataTypes.BYTE, (byte) 0xFF)); // Show all skin layers

                    return data;
                }
        );

        DigitalNPC.sendPacket(player, entityMetadata);
    }

    public static void sendTeleportPacket(Player player, NPC npc, org.bukkit.Location from) {
        if (npc.getLocation().distance(from) < 8) {
            WrapperPlayServerEntityRelativeMoveAndRotation movePacket = new WrapperPlayServerEntityRelativeMoveAndRotation(
                    npc.getEntityId(),
                    npc.getLocation().getX() - from.getX(),
                    npc.getLocation().getY() - from.getY(),
                    npc.getLocation().getZ() - from.getZ(),
                    npc.getLocation().getYaw(),
                    npc.getLocation().getPitch(),
                    true
            );

            DigitalNPC.sendPacket(player, movePacket);
            sendEntityHeadRotationPacket(player, npc);
        } else {
            if (DigitalNPC.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
                WrapperPlayServerEntityPositionSync teleportPacket = new WrapperPlayServerEntityPositionSync(
                        npc.getEntityId(),
                        new EntityPositionData(
                                new Vector3d(
                                        npc.getLocation().getX(),
                                        npc.getLocation().getY(),
                                        npc.getLocation().getZ()
                                ),
                                new Vector3d(),
                                npc.getLocation().getYaw(),
                                npc.getLocation().getPitch()
                        ),
                        true
                );

                DigitalNPC.sendPacket(player, teleportPacket);
            } else {
                WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                        npc.getEntityId(),
                        SpigotConversionUtil.fromBukkitLocation(npc.getLocation()),
                        true
                );

                DigitalNPC.sendPacket(player, teleportPacket);
            }

            sendEntityHeadRotationPacket(player, npc);
        }
    }

    public static void sendAnimationPacket(Player player, NPC npc, WrapperPlayServerEntityAnimation.EntityAnimationType animation) {
        WrapperPlayServerEntityAnimation animationPacket = new WrapperPlayServerEntityAnimation(
                npc.getEntityId(),
                animation
        );

        DigitalNPC.sendPacket(player, animationPacket);
    }

    public static void sendEntityEquipmentPacket(Player player, NPC npc) {
        if (DigitalNPC.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16)) {
            WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(npc.getEntityId(), new ArrayList<>());

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!npc.getItems().containsKey(slot)) {
                    equipmentPacket.getEquipment().add(new Equipment(slot, ItemStack.EMPTY));
                } else {
                    equipmentPacket.getEquipment().add(new Equipment(slot, SpigotConversionUtil.fromBukkitItemStack(npc.getItems().get(slot))));
                }
            }

            DigitalNPC.sendPacket(player, equipmentPacket);
        } else {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(
                        npc.getEntityId(),
                        List.of(new Equipment(
                                slot,
                                npc.getItems().containsKey(slot)
                                        ? SpigotConversionUtil.fromBukkitItemStack(npc.getItems().get(slot))
                                        : ItemStack.EMPTY
                        ))
                );

                DigitalNPC.sendPacket(player, equipmentPacket);
            }
        }
    }

    public static void sendPlayerInfoRemovePacket(Player player, NPC npc) {
        if (DigitalNPC.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            WrapperPlayServerPlayerInfoRemove playerInfoRemove = new WrapperPlayServerPlayerInfoRemove(Collections.singletonList(npc.getUuid()));

            DigitalNPC.sendPacket(player, playerInfoRemove);
        } else {
            WrapperPlayServerPlayerInfo playerInfoRemove = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, Collections.singletonList(
                    new WrapperPlayServerPlayerInfo.PlayerData(
                            null,
                            new UserProfile(npc.getUuid(), npc.getName()),
                            GameMode.SURVIVAL,
                            1
                    )
            ));

            DigitalNPC.sendPacket(player, playerInfoRemove);
        }
    }

    public static void sendEntityDestroyPacket(Player player, NPC npc) {
        WrapperPlayServerDestroyEntities destroyEntity = new WrapperPlayServerDestroyEntities(npc.getEntityId());

        DigitalNPC.sendPacket(player, destroyEntity);
    }
}
