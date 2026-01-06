package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

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
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
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
            if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
                if (name != null) {
                    parts.add("\"minecraft:custom_name\":\"" + escapeString(name.getString()) + "\"");
                }
            }
            
            // Item Name (unterschiedlich von Custom Name)
            if (stack.contains(DataComponentTypes.ITEM_NAME)) {
                Text name = stack.get(DataComponentTypes.ITEM_NAME);
                if (name != null) {
                    parts.add("\"minecraft:item_name\":\"" + escapeString(name.getString()) + "\"");
                }
            }
            
            // Damage
            if (stack.contains(DataComponentTypes.DAMAGE)) {
                Integer damage = stack.get(DataComponentTypes.DAMAGE);
                if (damage != null && damage > 0) {
                    parts.add("\"minecraft:damage\":" + damage);
                }
            }
            
            // Max Damage
            if (stack.contains(DataComponentTypes.MAX_DAMAGE)) {
                Integer maxDamage = stack.get(DataComponentTypes.MAX_DAMAGE);
                if (maxDamage != null) {
                    parts.add("\"minecraft:max_damage\":" + maxDamage);
                }
            }
            
            // Enchantments
            if (stack.contains(DataComponentTypes.ENCHANTMENTS)) {
                ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
                if (enchants != null && !enchants.isEmpty()) {
                    StringBuilder enchantSb = new StringBuilder("\"minecraft:enchantments\":{levels:{");
                    List<String> enchantList = new ArrayList<>();
                    for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
                        int level = enchants.getLevel(entry);
                        enchantList.add("\"" + entry.getIdAsString() + "\":" + level);
                    }
                    enchantSb.append(String.join(",", enchantList));
                    enchantSb.append("}}");
                    parts.add(enchantSb.toString());
                }
            }
            
            // Stored Enchantments (für Bücher)
            if (stack.contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
                ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
                if (enchants != null && !enchants.isEmpty()) {
                    StringBuilder enchantSb = new StringBuilder("\"minecraft:stored_enchantments\":{levels:{");
                    List<String> enchantList = new ArrayList<>();
                    for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
                        int level = enchants.getLevel(entry);
                        enchantList.add("\"" + entry.getIdAsString() + "\":" + level);
                    }
                    enchantSb.append(String.join(",", enchantList));
                    enchantSb.append("}}");
                    parts.add(enchantSb.toString());
                }
            }
            
            // Lore
            if (stack.contains(DataComponentTypes.LORE)) {
                LoreComponent lore = stack.get(DataComponentTypes.LORE);
                if (lore != null && !lore.lines().isEmpty()) {
                    StringBuilder loreSb = new StringBuilder("\"minecraft:lore\":[");
                    List<String> loreLines = new ArrayList<>();
                    for (Text line : lore.lines()) {
                        loreLines.add("\"" + escapeString(line.getString()) + "\"");
                    }
                    loreSb.append(String.join(",", loreLines));
                    loreSb.append("]");
                    parts.add(loreSb.toString());
                }
            }
            
            // Unbreakable
            if (stack.contains(DataComponentTypes.UNBREAKABLE)) {
                parts.add("\"minecraft:unbreakable\":{}");
            }
            
            // Custom Model Data
            if (stack.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
                CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
                if (cmd != null) {
                    parts.add("\"minecraft:custom_model_data\":" + cmd.toString());
                }
            }
            
            // Potion Contents
            if (stack.contains(DataComponentTypes.POTION_CONTENTS)) {
                PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (potion != null) {
                    StringBuilder potionSb = new StringBuilder("\"minecraft:potion_contents\":{");
                    if (potion.potion().isPresent()) {
                        potionSb.append("potion:\"").append(potion.potion().get().getIdAsString()).append("\"");
                    }
                    potionSb.append("}");
                    parts.add(potionSb.toString());
                }
            }
            
            // Dyed Color
            if (stack.contains(DataComponentTypes.DYED_COLOR)) {
                DyedColorComponent color = stack.get(DataComponentTypes.DYED_COLOR);
                if (color != null) {
                    parts.add("\"minecraft:dyed_color\":{rgb:" + color.rgb() + "}");
                }
            }
            
            // Custom Data (NBT)
            if (stack.contains(DataComponentTypes.CUSTOM_DATA)) {
                NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
                if (customData != null) {
                    NbtCompound nbt = customData.copyNbt();
                    if (!nbt.isEmpty()) {
                        parts.add("\"minecraft:custom_data\":" + nbt.asString());
                    }
                }
            }
            
            // Attribute Modifiers
            if (stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
                AttributeModifiersComponent attrs = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                if (attrs != null && !attrs.modifiers().isEmpty()) {
                    StringBuilder attrSb = new StringBuilder("\"minecraft:attribute_modifiers\":{modifiers:[");
                    List<String> attrList = new ArrayList<>();
                    for (AttributeModifiersComponent.Entry entry : attrs.modifiers()) {
                        attrList.add("{type:\"" + entry.attribute().getIdAsString() + 
                                    "\",id:\"" + entry.modifier().id() + 
                                    "\",amount:" + entry.modifier().value() + 
                                    ",operation:\"" + entry.modifier().operation().name() + "\"}");
                    }
                    attrSb.append(String.join(",", attrList));
                    attrSb.append("]}");
                    parts.add(attrSb.toString());
                }
            }
            
            // Container (für Shulker Boxes etc.)
            if (stack.contains(DataComponentTypes.CONTAINER)) {
                ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
                if (container != null) {
                    StringBuilder contSb = new StringBuilder("\"minecraft:container\":[");
                    List<String> items = new ArrayList<>();
                    int slot = 0;
                    for (ItemStack item : container.iterateNonEmpty()) {
                        items.add("{slot:" + slot + ",item:" + format(item) + "}");
                        slot++;
                    }
                    contSb.append(String.join(",", items));
                    contSb.append("]");
                    parts.add(contSb.toString());
                }
            }
            
            // Bundle Contents
            if (stack.contains(DataComponentTypes.BUNDLE_CONTENTS)) {
                BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
                if (bundle != null && !bundle.isEmpty()) {
                    StringBuilder bundleSb = new StringBuilder("\"minecraft:bundle_contents\":[");
                    List<String> items = new ArrayList<>();
                    for (ItemStack item : bundle.iterate()) {
                        items.add(format(item));
                    }
                    bundleSb.append(String.join(",", items));
                    bundleSb.append("]");
                    parts.add(bundleSb.toString());
                }
            }
            
            // Written Book Content
            if (stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
                WrittenBookContentComponent book = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
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
            if (stack.contains(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
                WritableBookContentComponent book = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
                if (book != null && !book.pages().isEmpty()) {
                    StringBuilder bookSb = new StringBuilder("\"minecraft:writable_book_content\":{pages:[");
                    List<String> pages = new ArrayList<>();
                    for (var page : book.pages()) {
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
    
    private static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
