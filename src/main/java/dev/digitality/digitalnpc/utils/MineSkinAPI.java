package dev.digitality.digitalnpc.utils;

import com.google.gson.JsonObject;
import dev.digitality.digitalnpc.DigitalNPC;
import dev.digitality.digitalnpc.api.NPC;
import lombok.Cleanup;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MineSkinAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(MineSkinAPI.class);
    private static final String API_URL = "https://api.mineskin.org/get/uuid/";

    private static final Map<String, NPC.Skin> skinCache = new HashMap<>();

    public static NPC.Skin fetchSkinFromUUID(String uuid) {
        if (skinCache.containsKey(uuid)) {
            return skinCache.get(uuid);
        }

        try {
            Request request = new Request.Builder()
                    .url(API_URL + uuid)
                    .header("Accept", "application/json")
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .header("User-Agent", "Minecraft Server Core")
                    .build();
            @Cleanup
            Response response = DigitalNPC.getHttpClient().newCall(request).execute();

            JsonObject jsonObject = DigitalNPC.getGson().fromJson(response.body().string(), JsonObject.class);
            JsonObject textures = jsonObject.get("data").getAsJsonObject().get("texture").getAsJsonObject();

            String value = textures.get("value").getAsString();
            String signature = textures.get("signature").getAsString();

            NPC.Skin skin = new NPC.Skin(value, signature);
            skinCache.put(uuid, skin);

            return skin;
        } catch (IOException ex) {
            LOGGER.error("Could not fetch skin! (UUID: {}).", uuid);
            ex.printStackTrace();
        }

        return null;
    }
}