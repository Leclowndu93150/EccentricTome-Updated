package website.eccentric.tome;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TomeScreen extends Screen {
    private static final int LEFT_CLICK = 0;

    private final ItemStack tome;

    private String mod;
    private String key;

    protected TomeScreen(ItemStack tome) {
        super(new TextComponent(""));
        this.tome = tome;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button != LEFT_CLICK || mod == null) return super.mouseClicked(x, y, button);

        EccentricTome.CHANNEL.sendToServer(new ConvertMessage(mod, key));
        this.minecraft.setScreen(null);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float ticks) { 
        super.render(poseStack, mouseX, mouseY, ticks);

        var mods = Tag.getMods(tome);
        var window = minecraft.getWindow();
        var booksPerRow = 6;
        var rows = mods.size() / booksPerRow + 1;
        var iconSize = 20;
        var startX = window.getGuiScaledWidth() / 2 - booksPerRow * iconSize / 2;
        var startY = window.getGuiScaledHeight() / 2 - rows * iconSize + 45;
        var padding = 4;
        fill(poseStack, startX - padding, startY - padding, startX + iconSize * booksPerRow + padding, startY + iconSize * rows + padding, 0x22000000);

        this.mod = null;
        String name = null;
        var index = 0;
        for (var mod : mods.getAllKeys()) {
            var books = mods.getCompound(mod);
            for (var key : books.getAllKeys()) {
                var book = ItemStack.of(books.getCompound(key));
                if (book.is(Items.AIR)) continue;

                var stackX = startX + (index % booksPerRow) * iconSize;
                var stackY = startY + (index / booksPerRow) * iconSize;

                if (mouseX > stackX && mouseY > stackY && mouseX <= (stackX + 16) && mouseY <= (stackY + 16)) {
                    this.mod = mod;
                    this.key = key;
                    name = book.getHoverName().getString();
                }

                minecraft.getItemRenderer().renderAndDecorateItem(book, stackX, stackY);
                index++;
            }
        }

        if (this.mod != null) {
            var tooltips = List.of(new TextComponent(name), new TextComponent(ChatFormatting.GRAY + Mod.name(this.mod)));
            renderComponentTooltip(poseStack, tooltips, mouseX, mouseY, font);
        }
    }

}
