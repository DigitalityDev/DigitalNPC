package dev.digitality.digitalnpc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.gson.Gson;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import dev.digitality.digitalnpc.api.NPC;
import dev.digitality.digitalnpc.listeners.ChunkListener;
import dev.digitality.digitalnpc.listeners.NPCPacketListener;
import dev.digitality.digitalnpc.listeners.PlayerListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;

public class DigitalNPC {
    private static final Logger LOGGER = LoggerFactory.getLogger(DigitalNPC.class);

    @Getter
    private static final Random random = new Random();
    @Getter
    private static final Gson gson = new Gson();
    @Getter
    private static final OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(true).build();
    @Getter
    private static Plugin plugin = null;

    @Getter
    private static final List<NPC> npcList = new ArrayList<>();
    @Getter @Setter
    private static final double autoHideDistance = 50.0;

    /**
     * Important to call in onEnable to register the NPC listener.
     *
     * @param plugin The plugin instance.
     */
    public static void register(Plugin plugin) {
        DigitalNPC.plugin = plugin;

        if (Arrays.equals(DigitalNPC.class.getPackage().getName().split("\\."), new String[] {"dev", "digitality", "digitalnpc"})) // Relocation relocates strings too, so we have to use the array method. Credits: Item-NBT-API, bStats
            LOGGER.warn("DigitalNPC was shaded but not transformed! This is prone to errors! Please nag the author of {} to use the relocation according to README!", plugin.getName());

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().getSettings().checkForUpdates(false);
        PacketEvents.getAPI().load();


        PacketEvents.getAPI().getEventManager().registerListener(new NPCPacketListener());

        PacketEvents.getAPI().init();

        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChunkListener(), plugin);
    }

    public static void sendPacket(Player p, PacketWrapper<?> packet) {
        if (plugin == null) {
            LOGGER.error("Plugin instance is not set! Please call DigitalNPC.register(plugin) in your onEnable method!");
            return;
        }

        PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
    }

    public static ServerVersion getServerVersion() {
        return PacketEvents.getAPI().getServerManager().getVersion();
    }
}
