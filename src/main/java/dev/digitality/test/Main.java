package dev.digitality.test;

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.NPC;
import dev.digitality.digitalnpc.api.enums.NPCState;

public class Main extends JavaPlugin implements CommandExecutor {
    @Override
    public void onEnable() {
        DigitalNPC.register(this);

        getCommand("dnpc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            NPC npc = new NPC(((Player) sender).getLocation());
            npc.show();

            sender.sendMessage("Spawned NPC with name " + npc.getName() + " and UUID " + npc.getUuid());
        } else if (args[0].equalsIgnoreCase("skin")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.setSkin(args[1]);
            }
        } else if (args[0].equalsIgnoreCase("crouch")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.toggleState(NPCState.CROUCHED);
            }
        } else if (args[0].equalsIgnoreCase("pose")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.setPose(EntityPose.valueOf(args[1]));
            }
        } else if (args[0].equalsIgnoreCase("animate")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.playAnimation(WrapperPlayServerEntityAnimation.EntityAnimationType.valueOf(args[1]));
            }
        } else if (args[0].equalsIgnoreCase("move")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.setLocation(((Player) sender).getLocation());
            }
        } else if (args[0].equalsIgnoreCase("look")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.lookAt(((Player) sender).getLocation());
            }
        } else if (args[0].equalsIgnoreCase("item")) {
            for (NPC npc : DigitalNPC.getNpcList().stream().filter(n -> n.getShown().contains(sender)).collect(Collectors.toList())) {
                npc.setItem(EquipmentSlot.MAIN_HAND, ((Player) sender).getInventory().getItem(0));
            }
        }

        return true;
    }
}
