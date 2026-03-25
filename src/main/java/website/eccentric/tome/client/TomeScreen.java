package website.eccentric.tome.client;

import java.util.Collection;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import website.eccentric.tome.EccentricDataComponents;
import website.eccentric.tome.EccentricTome;
import website.eccentric.tome.ModName;
import website.eccentric.tome.core.TomeData;
import website.eccentric.tome.network.SelectBookPacket;

public class TomeScreen extends Screen {
    private static final int LEFT_CLICK = 0;

    private final ItemStack tome;
    private String selectedMod;
    private int selectedIndex;

    public TomeScreen(ItemStack tome) {
        super(Component.empty());
        this.tome = tome;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != LEFT_CLICK || selectedMod == null)
            return super.mouseClicked(event, doubleClick);

        ClientPacketDistributor.sendToServer(new SelectBookPacket(selectedMod, selectedIndex));

        this.onClose();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        var minecraft = this.minecraft;
        var key = InputConstants.getKey(event);
        if (super.keyPressed(event)) {
            return true;
        } else if (minecraft != null && minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float ticks) {
        var minecraft = this.minecraft;
        if (minecraft == null)
            return;

        super.extractRenderState(graphics, mouseX, mouseY, ticks);

        TomeData data = tome.getOrDefault(EccentricDataComponents.TOME_DATA.get(), TomeData.EMPTY);
        var books = data.books().values().stream()
                .flatMap(Collection::stream)
                .toList();

        var window = minecraft.getWindow();
        var booksPerRow = 6;
        var rows = books.size() / booksPerRow + 1;
        var iconSize = 20;
        var startX = window.getGuiScaledWidth() / 2 - booksPerRow * iconSize / 2;
        var startY = window.getGuiScaledHeight() / 2 - rows * iconSize + 45;
        var padding = 4;
        graphics.fill(startX - padding, startY - padding,
                startX + iconSize * booksPerRow + padding,
                startY + iconSize * rows + padding, 0x22000000);

        this.selectedMod = null;
        this.selectedIndex = -1;

        var displayIndex = 0;
        for (var entry : data.books().entrySet()) {
            var modId = entry.getKey();
            var modBooks = entry.getValue();

            for (var bookIndex = 0; bookIndex < modBooks.size(); bookIndex++) {
                var book = modBooks.get(bookIndex);
                if (book.is(Items.AIR))
                    continue;

                var stackX = startX + (displayIndex % booksPerRow) * iconSize;
                var stackY = startY + (displayIndex / booksPerRow) * iconSize;

                if (mouseX > stackX && mouseY > stackY && mouseX <= (stackX + 16) && mouseY <= (stackY + 16)) {
                    this.selectedMod = modId;
                    this.selectedIndex = bookIndex;
                    graphics.setTooltipForNextFrame(this.font, book, mouseX, mouseY);
                }

                graphics.item(book, stackX, stackY);
                graphics.itemDecorations(font, book, mouseX, mouseY);
                displayIndex++;
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }
}
