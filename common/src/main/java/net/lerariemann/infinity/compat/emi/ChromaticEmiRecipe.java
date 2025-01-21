package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ChromaticEmiRecipe extends BasicEmiRecipe {
    Identifier infoId;

    public static ChromaticEmiRecipe of(String id, Item item) {
        return new ChromaticEmiRecipe(InfinityMethods.getId("/"+id), item, null);
    }
    public static ChromaticEmiRecipe withInfo(String id, Item item) {
        return new ChromaticEmiRecipe(InfinityMethods.getId("/"+id), item, InfinityMethods.getId(id));
    }

    public ChromaticEmiRecipe(Identifier id, Item item, @Nullable Identifier infoId) {
        super(EmiCompat.CHROMATIC, id, 140, 18);
        this.inputs.add(EmiIngredient.of(Ingredient.ofItems(item)));
        this.infoId = infoId;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        MutableText text = Text.translatable(Util.createTranslationKey("chromatic", this.id));
        Map<Integer, MutableText> chars = new HashMap<>();
        text.asOrderedText().accept((index, style, codepoint) -> {
            chars.put(index, Text.literal(String.valueOf(Character.toChars(codepoint))).fillStyle(style));
            return true;
        });
        int len = chars.size();
        MutableText finalText = Text.empty();
        for (int i = 0; i < len; i++) {
            MutableText ch = chars.get(i);
            if (ch == null) continue;
            var chStyle = ch.setStyle(Style.EMPTY.withColor(getCharColor(i, len)));
            finalText.append(chStyle);
        }
        if (infoId != null)
            widgets.add(new TextWidgetWithTooltip(Text.literal("ℹ").formatted(Formatting.GRAY)
                    .asOrderedText(), 128, 5, 0xFFFFFF, false,
                    Text.translatable(Util.createTranslationKey("chromatic_info", this.infoId))));
        widgets.addText(finalText, 24, 5, 0xFFFFFF, false);
    }

    public int getCharColor(int chnum, int len) {
        float hue = 0.7417f;
        float saturation;
        float brightness;
        if (id.getPath().contains("hue")) {
            hue = chnum / (float)len;
            saturation = 1.0f;
            brightness = 0.85f;
        }
        else if (id.getPath().contains("saturation")) {
            saturation = 0.6157f * chnum / (float)len;
            if (id.getPath().contains("minus")) saturation = 0.6157f - saturation;
            brightness = 0.5f;
        }
        else if (id.getPath().contains("brightness")) {
            brightness = 0.8588f * chnum / (float)len;
            if (id.getPath().contains("minus")) brightness = 0.8588f - brightness;
            saturation = 0.6157f;
        }
        else return ColorLogic.defaultChromatic;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static class TextWidgetWithTooltip extends TextWidget {
        Text tooltip;

        public TextWidgetWithTooltip(OrderedText text, int x, int y, int color, boolean shadow, Text tooltip) {
            super(text, x, y, color, shadow);
            this.tooltip = tooltip;
        }
        public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
            return List.of(TooltipComponent.of(tooltip.asOrderedText()));
        }
    }
}
