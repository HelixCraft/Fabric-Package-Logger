package dev.redstone.packetlogger.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Zwei-Spalten Widget: Links alle verfügbaren Pakete, rechts die ausgewählten.
 * Mit Suchfunktion und Buttons zum Hinzufügen/Entfernen.
 */
public class DualListSelectorWidget implements Drawable, Element, Selectable {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final TextRenderer textRenderer;
    private final String title;
    
    private final TextFieldWidget searchField;
    private final List<String> allPackages;
    private final Set<String> selectedPackages;
    private final Consumer<Set<String>> onSelectionChanged;
    
    private List<String> filteredAvailable;
    private List<String> filteredSelected;
    private int leftScrollOffset = 0;
    private int rightScrollOffset = 0;
    private final int itemHeight = 14;
    private int hoveredLeftIndex = -1;
    private int hoveredRightIndex = -1;
    private boolean focused = false;
    
    private static final int HEADER_HEIGHT = 50;
    private static final int PADDING = 4;
    private static final int GAP = 8;
    private static final int SEARCH_FIELD_Y_OFFSET = 18; // Position des Suchfelds unter dem Titel
    private static final int SEARCH_FIELD_HEIGHT = 20;
    
    public DualListSelectorWidget(int x, int y, int width, int height, String title,
                                   List<String> packages, Set<String> initialSelection,
                                   Consumer<Set<String>> onSelectionChanged) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.allPackages = new ArrayList<>(packages);
        this.selectedPackages = new HashSet<>(initialSelection);
        this.onSelectionChanged = onSelectionChanged;
        
        // Suchfeld - Position unter dem Titel
        this.searchField = new TextFieldWidget(
            textRenderer, 
            x + PADDING, 
            y + SEARCH_FIELD_Y_OFFSET, 
            width - PADDING * 2, 
            SEARCH_FIELD_HEIGHT, 
            Text.literal("Search")
        );
        this.searchField.setPlaceholder(Text.literal("Search packages..."));
        this.searchField.setChangedListener(this::onSearchChanged);
        this.searchField.setDrawsBackground(true);
        this.searchField.setEditable(true);
        this.searchField.setMaxLength(256);
        
        updateFilteredLists();
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    // Getter für das Suchfeld - wird von SimpleConfigScreen benötigt
    public int getSearchFieldX() {
        return x + PADDING;
    }
    
    public int getSearchFieldY() {
        return y + SEARCH_FIELD_Y_OFFSET;
    }
    
    public int getSearchFieldWidth() {
        return width - PADDING * 2;
    }
    
    public int getSearchFieldHeight() {
        return SEARCH_FIELD_HEIGHT;
    }
    
    private void onSearchChanged(String query) {
        updateFilteredLists();
        leftScrollOffset = 0;
        rightScrollOffset = 0;
    }
    
    private void updateFilteredLists() {
        String query = searchField.getText().toLowerCase();
        
        filteredAvailable = new ArrayList<>();
        filteredSelected = new ArrayList<>();
        
        for (String pkg : allPackages) {
            boolean matchesQuery = query.isEmpty() || pkg.toLowerCase().contains(query);
            if (matchesQuery) {
                if (selectedPackages.contains(pkg)) {
                    filteredSelected.add(pkg);
                } else {
                    filteredAvailable.add(pkg);
                }
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int listWidth = (width - GAP) / 2;
        int listY = y + HEADER_HEIGHT;
        int listHeight = height - HEADER_HEIGHT - PADDING;
        int visibleItems = listHeight / itemHeight;
        
        // Hintergrund Panel
        context.fill(x, y, x + width, y + height, 0xC0101010);
        context.drawBorder(x, y, width, height, 0xFF3A3A3A);
        
        // Titel
        context.drawText(textRenderer, title, x + PADDING, y + 2, 0xFFFFFF, false);
        
        // Suchfeld
        searchField.render(context, mouseX, mouseY, delta);
        
        // === LINKE LISTE (Verfügbar) ===
        int leftX = x + PADDING;
        renderListPanel(context, leftX, listY, listWidth - PADDING, listHeight, 
                       "Available", filteredAvailable, leftScrollOffset, 
                       mouseX, mouseY, true, visibleItems);
        
        // === RECHTE LISTE (Ausgewählt) ===
        int rightX = x + listWidth + GAP / 2;
        renderListPanel(context, rightX, listY, listWidth - PADDING, listHeight,
                       "Selected (" + selectedPackages.size() + ")", filteredSelected, rightScrollOffset,
                       mouseX, mouseY, false, visibleItems);
        
        // Hover-Index aktualisieren
        updateHoverIndices(mouseX, mouseY, leftX, rightX, listY, listWidth - PADDING, listHeight, visibleItems);
    }
    
    private void renderListPanel(DrawContext context, int px, int py, int pw, int ph,
                                  String label, List<String> items, int scrollOffset,
                                  int mouseX, int mouseY, boolean isLeft, int visibleItems) {
        // Panel Hintergrund
        context.fill(px, py, px + pw, py + ph, 0xFF1A1A1A);
        context.drawBorder(px, py, pw, ph, 0xFF4A4A4A);
        
        // Label
        context.drawText(textRenderer, label, px + 2, py - 10, 0xAAAAAA, false);
        
        // Items
        int itemY = py + 2;
        for (int i = 0; i < visibleItems && (i + scrollOffset) < items.size(); i++) {
            int index = i + scrollOffset;
            String pkg = items.get(index);
            int currentItemY = itemY + (i * itemHeight);
            
            boolean isHovered = (isLeft ? hoveredLeftIndex : hoveredRightIndex) == index;
            
            // Hover Hintergrund
            if (isHovered) {
                context.fill(px + 1, currentItemY, px + pw - 1, currentItemY + itemHeight, 
                           isLeft ? 0xFF2A4A2A : 0xFF4A2A2A);
            }
            
            // Icon (+ oder -) - zentriert vertikal (Minecraft font height ist 9)
            String icon = isLeft ? "+" : "-";
            int iconColor = isLeft ? 0xFF55FF55 : 0xFFFF5555;
            int textYOffset = (itemHeight - 9) / 2;
            context.drawText(textRenderer, icon, px + 4, currentItemY + textYOffset, iconColor, false);
            
            // Package Name (gekürzt) - zentriert vertikal
            String displayText = getShortPackageName(pkg, pw - 18);
            context.drawText(textRenderer, displayText, px + 14, currentItemY + textYOffset, 0xDDDDDD, false);
        }
        
        // Scrollbar
        if (items.size() > visibleItems) {
            int scrollbarHeight = Math.max(10, (int)((float)visibleItems / items.size() * (ph - 4)));
            int maxScroll = items.size() - visibleItems;
            int scrollbarY = py + 2 + (int)((float)scrollOffset / maxScroll * (ph - 4 - scrollbarHeight));
            
            context.fill(px + pw - 4, py + 2, px + pw - 1, py + ph - 2, 0xFF0A0A0A);
            context.fill(px + pw - 3, scrollbarY, px + pw - 1, scrollbarY + scrollbarHeight, 0xFF555555);
        }
    }
    
    private String getShortPackageName(String pkg, int maxWidth) {
        if (textRenderer.getWidth(pkg) <= maxWidth) {
            return pkg;
        }
        // Zeige nur den letzten Teil des Paketnamens
        String[] parts = pkg.split("\\.");
        StringBuilder result = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            String test = (i < parts.length - 1 ? "..." : "") + 
                         String.join(".", java.util.Arrays.copyOfRange(parts, i, parts.length));
            if (textRenderer.getWidth(test) <= maxWidth) {
                result = new StringBuilder(test);
            } else {
                break;
            }
        }
        return result.length() > 0 ? result.toString() : textRenderer.trimToWidth(pkg, maxWidth - 10) + "...";
    }
    
    private void updateHoverIndices(int mouseX, int mouseY, int leftX, int rightX, 
                                     int listY, int listWidth, int listHeight, int visibleItems) {
        hoveredLeftIndex = -1;
        hoveredRightIndex = -1;
        
        if (mouseY >= listY && mouseY < listY + listHeight) {
            int relativeY = mouseY - listY - 2;
            int itemIndex = relativeY / itemHeight;
            
            if (mouseX >= leftX && mouseX < leftX + listWidth && itemIndex >= 0) {
                int actualIndex = itemIndex + leftScrollOffset;
                if (actualIndex < filteredAvailable.size()) {
                    hoveredLeftIndex = actualIndex;
                }
            } else if (mouseX >= rightX && mouseX < rightX + listWidth && itemIndex >= 0) {
                int actualIndex = itemIndex + rightScrollOffset;
                if (actualIndex < filteredSelected.size()) {
                    hoveredRightIndex = actualIndex;
                }
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if click is on search field (use our known coordinates)
        int sfX = x + PADDING;
        int sfY = y + SEARCH_FIELD_Y_OFFSET;
        int sfW = width - PADDING * 2;
        int sfH = SEARCH_FIELD_HEIGHT;
        
        if (mouseX >= sfX && mouseX < sfX + sfW && mouseY >= sfY && mouseY < sfY + sfH) {
            searchField.setFocused(true);
            this.focused = true;
            searchField.mouseClicked(mouseX, mouseY, button);
            return true;
        } else {
            searchField.setFocused(false);
        }
        
        // Check if click is within widget bounds
        boolean withinBounds = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        
        // Calculate list dimensions for click detection
        int listWidth = (width - GAP) / 2;
        int listY = y + HEADER_HEIGHT;
        int listHeight = height - HEADER_HEIGHT - PADDING;
        int visibleItems = listHeight / itemHeight;
        int leftX = x + PADDING;
        int rightX = x + listWidth + GAP / 2;
        int actualListWidth = listWidth - PADDING;
        
        // Check if click is in list area
        if (mouseY >= listY && mouseY < listY + listHeight) {
            int relativeY = (int)mouseY - listY - 2;
            int itemIndex = relativeY / itemHeight;
            
            if (itemIndex >= 0 && itemIndex < visibleItems) {
                // Check left list (available packages)
                if (mouseX >= leftX && mouseX < leftX + actualListWidth) {
                    int actualIndex = itemIndex + leftScrollOffset;
                    if (actualIndex >= 0 && actualIndex < filteredAvailable.size()) {
                        String pkg = filteredAvailable.get(actualIndex);
                        selectedPackages.add(pkg);
                        updateFilteredLists();
                        onSelectionChanged.accept(selectedPackages);
                        return true;
                    }
                }
                // Check right list (selected packages)
                else if (mouseX >= rightX && mouseX < rightX + actualListWidth) {
                    int actualIndex = itemIndex + rightScrollOffset;
                    if (actualIndex >= 0 && actualIndex < filteredSelected.size()) {
                        String pkg = filteredSelected.get(actualIndex);
                        selectedPackages.remove(pkg);
                        updateFilteredLists();
                        onSelectionChanged.accept(selectedPackages);
                        return true;
                    }
                }
            }
        }
        
        return withinBounds;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listWidth = (width - GAP) / 2;
        int listY = y + HEADER_HEIGHT;
        int listHeight = height - HEADER_HEIGHT - PADDING;
        int visibleItems = listHeight / itemHeight;
        
        int leftX = x + PADDING;
        int rightX = x + listWidth + GAP / 2;
        
        if (mouseY >= listY && mouseY < listY + listHeight) {
            if (mouseX >= leftX && mouseX < leftX + listWidth) {
                int maxScroll = Math.max(0, filteredAvailable.size() - visibleItems);
                leftScrollOffset = Math.max(0, Math.min(maxScroll, leftScrollOffset - (int)verticalAmount));
                return true;
            } else if (mouseX >= rightX && mouseX < rightX + listWidth) {
                int maxScroll = Math.max(0, filteredSelected.size() - visibleItems);
                rightScrollOffset = Math.max(0, Math.min(maxScroll, rightScrollOffset - (int)verticalAmount));
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return searchField.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        return searchField.charTyped(chr, modifiers);
    }
    
    public TextFieldWidget getSearchField() {
        return searchField;
    }
    
    public Set<String> getSelectedPackages() {
        return new HashSet<>(selectedPackages);
    }
    
    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (!focused) {
            searchField.setFocused(false);
        }
    }
    
    @Override
    public boolean isFocused() {
        return focused || searchField.isFocused();
    }
    
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }
    
    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
