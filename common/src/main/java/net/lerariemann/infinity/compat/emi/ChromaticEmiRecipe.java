package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;

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
        widgets.addSlot(inputs.getFirst(), 0, 0);
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
            finalText.append(ch.withColor(getCharColor(i, len)));
        }
        if (infoId != null)
            EmiCompat.addInfo(widgets, 128, 5, Text.translatable(Util.createTranslationKey("recipe_info.chromatic", this.infoId)));
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
}
