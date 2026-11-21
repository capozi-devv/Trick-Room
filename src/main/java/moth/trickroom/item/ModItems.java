package moth.trickroom.item;

import moth.trickroom.TrickRoomMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModItems {
    public static final Item TRICK_ROOM = register("trick_room", new TrickRoomItem(settings("trick_room").maxCount(1).fireproof()));
    public static final Item CRACKED_TRICK_ROOM = register("cracked_trick_room", new TrickRoomItem(settings("cracked_trick_room").maxCount(1).fireproof()));

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(TRICK_ROOM));
    }

    private static Item register(String path, Item item) {
        return Registry.register(Registries.ITEM, TrickRoomMod.id(path), item);
    }

    private static Item.Settings settings(String path) {
        return new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, TrickRoomMod.id(path)));
    }
}
