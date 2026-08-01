package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatiert ItemStacks mit allen Components/NBT-Daten im JSON-ähnlichen Format.
 */
public class ItemStackFormatter {

    /**
     * Formatiert einen ItemStack im Minecraft-NBT-Stil.
     * Format: {id:"minecraft:diamond_sword",count:1,components:{...}}
     */
    public static String format(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "{id:\"minecraft:air\",count:0}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // ID
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        sb.append("id:\"").append(itemId).append("\"");

        // Count
        sb.append(",count:").append(stack.getCount());

        // Components
        String components = formatComponents(stack);
        if (!components.isEmpty()) {
            sb.append(",components:{").append(components).append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Formatiert einen ItemStack für Container-Slot-Format.
     * Format: {item:{id:"...",count:...},slot:X}
     */
    public static String formatForSlot(ItemStack stack, int slot) {
        if (stack == null || stack.isEmpty()) {
            return null; // Leere Slots werden ignoriert
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{item:").append(format(stack));
        sb.append(",slot:").append(slot).append("}");
        return sb.toString();
    }

    private static String formatComponents(ItemStack stack) {
        List<String> parts = new ArrayList<>();

        try {
            // Custom Name
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                Component name = stack.get(DataComponents.CUSTOM_NAME);
                if (name != null) {
                    parts.add("\"minecraft:custom_name\":\"" + escapeString(name.getString()) + "\"");
                }
            }

            // Item Name (unterschiedlich von Custom Name)
            if (stack.has(DataComponents.ITEM_NAME)) {
                Component name = stack.get(DataComponents.ITEM_NAME);
                if (name != null) {
                    parts.add("\"minecraft:item_name\":\"" + escapeString(name.getString()) + "\"");
                }
            }

            // Damage
            if (stack.has(DataComponents.DAMAGE)) {
                Integer damage = stack.get(DataComponents.DAMAGE);
                if (damage != null && damage > 0) {
                    parts.add("\"minecraft:damage\":" + damage);
                }
            }

            // Max Damage
            if (stack.has(DataComponents.MAX_DAMAGE)) {
                Integer maxDamage = stack.get(DataComponents.MAX_DAMAGE);
                if (maxDamage != null) {
                    parts.add("\"minecraft:max_damage\":" + maxDamage);
                }
            }

            // Enchantments
            if (stack.has(DataComponents.ENCHANTMENTS)) {
                ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
                if (enchants != null && !enchants.isEmpty()) {
                    parts.add(formatEnchantments("minecraft:enchantments", enchants));
                }
            }

            // Stored Enchantments (für Bücher)
            if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
                ItemEnchantments enchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
                if (enchants != null && !enchants.isEmpty()) {
                    parts.add(formatEnchantments("minecraft:stored_enchantments", enchants));
                }
            }

            // Lore
            if (stack.has(DataComponents.LORE)) {
                ItemLore lore = stack.get(DataComponents.LORE);
                if (lore != null && !lore.lines().isEmpty()) {
                    StringBuilder loreSb = new StringBuilder("\"minecraft:lore\":[");
                    List<String> loreLines = new ArrayList<>();
                    for (Component line : lore.lines()) {
                        loreLines.add("\"" + escapeString(line.getString()) + "\"");
                    }
                    loreSb.append(String.join(",", loreLines));
                    loreSb.append("]");
                    parts.add(loreSb.toString());
                }
            }

            // Unbreakable
            if (stack.has(DataComponents.UNBREAKABLE)) {
                parts.add("\"minecraft:unbreakable\":{}");
            }

            // Custom Model Data
            if (stack.has(DataComponents.CUSTOM_MODEL_DATA)) {
                CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
                if (cmd != null) {
                    parts.add("\"minecraft:custom_model_data\":\"" + escapeString(cmd.toString()) + "\"");
                }
            }

            // Potion Contents
            if (stack.has(DataComponents.POTION_CONTENTS)) {
                PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                if (potion != null) {
                    StringBuilder potionSb = new StringBuilder("\"minecraft:potion_contents\":{");
                    if (potion.potion().isPresent()) {
                        potionSb.append("potion:\"").append(potion.potion().get().getRegisteredName()).append("\"");
                    }
                    potionSb.append("}");
                    parts.add(potionSb.toString());
                }
            }

            // Dyed Color
            if (stack.has(DataComponents.DYED_COLOR)) {
                DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
                if (color != null) {
                    parts.add("\"minecraft:dyed_color\":{rgb:" + color.rgb() + "}");
                }
            }

            // Custom Data (NBT)
            if (stack.has(DataComponents.CUSTOM_DATA)) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    CompoundTag nbt = customData.copyTag();
                    if (!nbt.isEmpty()) {
                        parts.add("\"minecraft:custom_data\":" + nbt.toString());
                    }
                }
            }

            // Attribute Modifiers
            if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                ItemAttributeModifiers attrs = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (attrs != null && !attrs.modifiers().isEmpty()) {
                    StringBuilder attrSb = new StringBuilder("\"minecraft:attribute_modifiers\":{modifiers:[");
                    List<String> attrList = new ArrayList<>();
                    for (ItemAttributeModifiers.Entry entry : attrs.modifiers()) {
                        attrList.add("{type:\"" + entry.attribute().getRegisteredName() +
                                    "\",id:\"" + entry.modifier().id() +
                                    "\",amount:" + entry.modifier().amount() +
                                    ",operation:\"" + entry.modifier().operation().name() + "\"}");
                    }
                    attrSb.append(String.join(",", attrList));
                    attrSb.append("]}");
                    parts.add(attrSb.toString());
                }
            }

            // Container (für Shulker Boxes etc.)
            if (stack.has(DataComponents.CONTAINER)) {
                ItemContainerContents container = stack.get(DataComponents.CONTAINER);
                if (container != null) {
                    StringBuilder contSb = new StringBuilder("\"minecraft:container\":[");
                    List<String> items = new ArrayList<>();
                    List<ItemStack> contItems = container.nonEmptyItemCopyStream().toList();
                    int slot = 0;
                    for (ItemStack item : contItems) {
                        items.add("{slot:" + slot + ",item:" + format(item) + "}");
                        slot++;
                    }
                    contSb.append(String.join(",", items));
                    contSb.append("]");
                    parts.add(contSb.toString());
                }
            }

            // Bundle Contents
            if (stack.has(DataComponents.BUNDLE_CONTENTS)) {
                BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
                if (bundle != null && !bundle.isEmpty()) {
                    StringBuilder bundleSb = new StringBuilder("\"minecraft:bundle_contents\":[");
                    List<String> items = new ArrayList<>();
                    for (ItemStack item : bundle.itemCopyStream().toList()) {
                        items.add(format(item));
                    }
                    bundleSb.append(String.join(",", items));
                    bundleSb.append("]");
                    parts.add(bundleSb.toString());
                }
            }

            // Written Book Content
            if (stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
                WrittenBookContent book = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
                if (book != null) {
                    StringBuilder bookSb = new StringBuilder("\"minecraft:written_book_content\":{");
                    bookSb.append("title:\"").append(escapeString(book.title().raw())).append("\"");
                    bookSb.append(",author:\"").append(escapeString(book.author())).append("\"");
                    bookSb.append(",generation:").append(book.generation());
                    bookSb.append("}");
                    parts.add(bookSb.toString());
                }
            }

            // Writable Book Content
            if (stack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
                WritableBookContent book = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
                if (book != null && !book.pages().isEmpty()) {
                    StringBuilder bookSb = new StringBuilder("\"minecraft:writable_book_content\":{pages:[");
                    List<String> pages = new ArrayList<>();
                    for (Filterable<String> page : book.pages()) {
                        pages.add("\"" + escapeString(page.raw()) + "\"");
                    }
                    bookSb.append(String.join(",", pages));
                    bookSb.append("]}");
                    parts.add(bookSb.toString());
                }
            }

        } catch (Exception e) {
            parts.add("\"error\":\"" + escapeString(e.getMessage()) + "\"");
        }

        return String.join(",", parts);
    }

    private static String formatEnchantments(String key, ItemEnchantments enchants) {
        StringBuilder enchantSb = new StringBuilder("\"" + key + "\":{levels:{");
        List<String> enchantList = new ArrayList<>();
        for (Holder<Enchantment> entry : enchants.keySet()) {
            int level = enchants.getLevel(entry);
            enchantList.add("\"" + entry.getRegisteredName() + "\":" + level);
        }
        enchantSb.append(String.join(",", enchantList));
        enchantSb.append("}}");
        return enchantSb.toString();
    }

    private static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
