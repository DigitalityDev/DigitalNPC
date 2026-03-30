package dev.digitality.digitalnpc.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.digitality.digitalnpc.api.NPC;
import lombok.Getter;

@Getter
public class NPCInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final NPC npc;
    private final Player player;
    private final ClickType clickType;

    public NPCInteractEvent(NPC npc, Player player, ClickType clickType) {
        this.npc = npc;
        this.player = player;
        this.clickType = clickType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ClickType {
        LEFT_CLICK, RIGHT_CLICK
    }
}
