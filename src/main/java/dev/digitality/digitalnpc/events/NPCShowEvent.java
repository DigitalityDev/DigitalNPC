package dev.digitality.digitalnpc.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.digitality.digitalnpc.api.NPC;
import lombok.Getter;
import lombok.Setter;

@Getter
public class NPCShowEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Setter
    private boolean cancelled = false;

    private final NPC npc;
    private final Player player;
    private final boolean automatic;

    public NPCShowEvent(NPC npc, Player player, boolean automatic) {
        this.npc = npc;
        this.player = player;
        this.automatic = automatic;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
