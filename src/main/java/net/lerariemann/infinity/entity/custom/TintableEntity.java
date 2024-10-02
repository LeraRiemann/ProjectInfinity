package net.lerariemann.infinity.entity.custom;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3f;

import java.awt.*;

public interface TintableEntity {
    boolean hasCustomName();
    Text getName();
    int getId();
    int getAge();

    default Vector3f colorFromInt(int i) {
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        return new Vector3f(f, g, h);
    }

    static int getColorJeb(int age, int id) {
        int n = age / 25 + id;
        int o = DyeColor.values().length;
        int p = n % o;
        int q = (n + 1) % o;
        float r = (float)(age % 25) / 25.0F;
        int s = SheepEntity.getRgbColor(DyeColor.byId(p));
        int t = SheepEntity.getRgbColor(DyeColor.byId(q));
        return ColorHelper.Argb.lerp(r, s, t);
    }

    default int getColorNamed() {
        if (hasCustomName()) {
            String s = getName().getString();
            if ("jeb_".equals(s)) {
                return TintableEntity.getColorJeb(getAge(), getId());
            }
            if ("hue".equals(s)) {
                int n = getAge() + 400*getId();
                float hue = n / 400.f;
                hue = hue - (int) hue;
                return Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();
            }
        }
        return -1;
    }
    default int getColorForRender() {
        int v = getColorNamed();
        if (v!=-1) return v;
        return ColorHelper.Argb.fullAlpha(this.getColorRaw());
    }

    default int getColorRaw() {
        return 0;
    }
}
