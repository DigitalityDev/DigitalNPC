package dev.digitality.digitalnpc.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;

@AllArgsConstructor
@Getter
public enum NPCState {
    STANDING((byte) 0x00),
    ON_FIRE((byte) 0x01),
    CROUCHED((byte) 0x02), // Only hides nametag
    SPRINTING((byte) 0x08), // Only shows particles
    SWIMMING((byte) 0x10),
    INVISIBLE((byte) 0x20),
    GLOWING((byte) 0x40),
    FLYING_WITH_ELYTRA((byte) 0x80);

    private final byte bit;

    public static byte getMasked(NPCState... states) {
        return getMasked(Arrays.asList(states));
    }

    public static byte getMasked(Collection<NPCState> states) {
        byte mask = 0;

        for (NPCState state : states) {
            mask |= state.getBit();
        }

        return mask;
    }
}