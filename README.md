# DigitalNPC

### Packet-based NPC system for Developers

If you like our project, please join our [Discord](https://discord.gg/rvWmR4scc)!

## Table of Contents

* [0. Features](#0-features)
* [1. Installation](#1-installation)
  + [Maven](#maven)
  + [Gradle](#gradle)
* [2. Shading + Relocating (Very important!)](#2-shading--relocating-very-important)
  + [Maven](#maven-1)
  + [Gradle](#gradle-1)
* [3. Setup](#3-setup)
  + [3.0 Registering the API](#30-registering-the-api)
* [4. NPC Usage](#4-npc-usage)
  + [4.0 Creating an NPC](#40-creating-an-npc)
  + [4.1 Showing and Hiding](#41-showing-and-hiding)
  + [4.2 Setting a Skin](#42-setting-a-skin)
  + [4.3 Hologram Text](#43-hologram-text)
  + [4.4 Equipment](#44-equipment)
  + [4.5 States](#45-states)
  + [4.6 Pose](#46-pose)
  + [4.7 Animations](#47-animations)
  + [4.8 Movement and Rotation](#48-movement-and-rotation)
* [5. Events](#5-events)
* [6. Configuration](#6-configuration)
* [7. Putting it all together](#7-putting-it-all-together)

## 0. Features

* Packet-based fake player NPCs (no real entity spawned)
* Custom skins via [MineSkin](https://mineskin.org/) UUID
* Per-player or global show/hide with auto-hide by distance and view angle
* Hologram text support via [DecentHolograms](https://github.com/DecentSoftware-eu/DecentHolograms)
* Equipment / item-in-hand support
* Entity states: crouching, glowing, invisible, on fire, swimming, elytra, sprinting
* Custom entity poses and animations
* Chunk-aware auto-hiding
* Cancellable `NPCShowEvent` / `NPCHideEvent` and `NPCInteractEvent` (left/right click)
* Multi-version support (1.8 → 1.21+)

## 1. Installation

### ⚠️ Requirements
This library requires [DecentHolograms](https://github.com/DecentSoftware-eu/DecentHolograms) to be present on your server. Make sure to install it before using DigitalNPC.

### Maven

```xml
<repositories>
    <repository>
        <id>digitality-repo-releases</id>
        <url>https://maven.digitality.dev</releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.digitality</groupId>
        <artifactId>digitalnpc</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven {
        name "digitalityRepoReleases"
        url "https://maven.digitality.dev/releases"
    }
}

dependencies {
    implementation "dev.digitality:digitalnpc:1.0.0"
}
```

## 2. Shading + Relocating (Very important!)

### Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.4</version>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>dev.digitality.digitalnpc</pattern>
                        <shadedPattern>your.package.here.digitalnpc</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle

```groovy
plugins {
    id "com.github.johnrengelman.shadow" version "9.3.1"
}

shadowJar {
    relocate "dev.digitality.digitalnpc", "your.package.here.digitalnpc"
}
```

## 3. Setup

### 3.0 Registering the API

To register the API, you need to call the `DigitalNPC.register()` method. You can (and should) do this in your `onEnable()` method. You need to do this only once.

```java
@Override
public void onEnable() {
    DigitalNPC.register(this);
}
```

## 4. NPC Usage

### 4.0 Creating an NPC

Creating an NPC is as simple as instantiating the `NPC` class. You can optionally pass a `Location` directly in the constructor.

```java
NPC npc = new NPC(location);
```

### 4.1 Showing and Hiding

You can show or hide an NPC to all online players, or to a specific player.

```java
npc.show();           // show to all online players
npc.show(player);     // show to a specific player

npc.hide();           // hide from all players
npc.hide(player);     // hide from a specific player
```

Auto-hide is handled automatically based on distance and the player's field of view. You do not need to manage this yourself.

### 4.2 Setting a Skin

You can set the NPC's skin using a [MineSkin](https://mineskin.org/) UUID. The skin is fetched asynchronously so it won't block the main thread.

```java
npc.setSkin("mineSkinUUID");
```

You can also pass the texture value and signature directly if you already have them.

```java
npc.setSkin(new NPC.Skin("value", "signature"));
```

### 4.3 Hologram Text

You can set floating text above the NPC. This uses [DecentHolograms](https://github.com/DecentSoftware-eu/DecentHolograms) under the hood.

```java
npc.setText(List.of("&6Shop NPC", "&7Click to open the shop"));
```

You can also set text for a specific player only.

```java
npc.setText(player, List.of("&aHello, " + player.getName() + "!"));
```

### 4.4 Equipment

You can equip items in any slot using the `setItem()` method.

```java
npc.setItem(EquipmentSlot.MAIN_HAND, new ItemStack(Material.DIAMOND_SWORD));
npc.setItem(EquipmentSlot.HEAD, new ItemStack(Material.DIAMOND_HELMET));
```

### 4.5 States

You can toggle visual states on the NPC. Calling `toggleState()` again on the same state removes it.

```java
npc.toggleState(NPCState.GLOWING);
npc.toggleState(NPCState.INVISIBLE);
npc.toggleState(NPCState.CROUCHED);
```

Available states: `STANDING`, `ON_FIRE`, `CROUCHED`, `SPRINTING`, `SWIMMING`, `INVISIBLE`, `GLOWING`, `FLYING_WITH_ELYTRA`

### 4.6 Pose

You can set a custom entity pose for the NPC.

```java
npc.setPose(EntityPose.SLEEPING);
```

### 4.7 Animations

You can play animations on the NPC, such as arm swings or taking damage.

```java
npc.playAnimation(WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM);
```

### 4.8 Movement and Rotation

You can teleport an NPC to a new location, or make it look at a target location.

```java
npc.setLocation(newLocation);   // teleport the NPC
npc.lookAt(targetLocation);     // rotate the NPC to face a location
```

## 5. Events

DigitalNPC provides three events you can listen to.

| Event | Cancellable | Description |
|---|---|---|
| `NPCShowEvent` | ✅ | Fired before an NPC is shown to a player |
| `NPCHideEvent` | ✅ | Fired before an NPC is hidden from a player |
| `NPCInteractEvent` | ❌ | Fired when a player left- or right-clicks an NPC |

```java
@EventHandler
public void onNPCInteract(NPCInteractEvent event) {
    if (event.getClickType() == NPCInteractEvent.ClickType.RIGHT_CLICK) {
        event.getPlayer().sendMessage("You right-clicked the NPC!");
    }
}
```

## 6. Configuration

You can configure the auto-hide distance. NPCs further away than this value will be automatically hidden from players.

```java
DigitalNPC.setAutoHideDistance(100.0); // default: 50.0 blocks
```

## 7. Putting it all together

Here is a final example which you can use as a reference.

```java
NPC npc = new NPC(location);

npc.setSkin("mineSkinUUID");
npc.setText(List.of("&6Shop NPC", "&7Right-click to open"));
npc.setItem(EquipmentSlot.MAIN_HAND, new ItemStack(Material.DIAMOND_SWORD));
npc.toggleState(NPCState.GLOWING);
npc.show();
```

```java
@EventHandler
public void onNPCInteract(NPCInteractEvent event) {
    if (event.getClickType() != NPCInteractEvent.ClickType.RIGHT_CLICK) return;

    Player player = event.getPlayer();
    player.sendMessage("&aWelcome to the shop, " + player.getName() + "!");
    player.openInventory(new ShopGUI(player).getInventory());
}
```
