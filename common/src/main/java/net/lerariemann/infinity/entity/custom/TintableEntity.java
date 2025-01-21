package net.lerariemann.infinity.entity.custom;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.joml.Vector3f;

import java.awt.*;

public interface TintableEntity {
    boolean hasCustomName();
    Text getName();
    int getId();
    int getAge();

    default Vector3f particleColorFromInt(int i) {
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        return new Vector3f(f, g, h);
    }


    default Vector3f getColorNamed() {
        if (hasCustomName()) {
            String s = getName().getString();
            if ("jeb_".equals(s)) {
                int n = getAge() / 25 + getId();
                int o = DyeColor.values().length;
                int p = n % o;
                int q = (n + 1) % o;
                float r = (getAge() % 25) / 25.0f;
                float[] fs = SheepEntity.getRgbColor(DyeColor.byId(p));
                float[] gs = SheepEntity.getRgbColor(DyeColor.byId(q));
                float f = fs[0] * (1.0f - r) + gs[0] * r;
                float g = fs[1] * (1.0f - r) + gs[1] * r;
                float h = fs[2] * (1.0f - r) + gs[2] * r;
                return new Vector3f(f, g, h);
            }
            if ("hue".equals(s)) {
                int n = getAge() + getId();
                float hue = n / 400.f;
                hue = hue - (int) hue;
                return particleColorFromInt(Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
            }
        }
        return null;
    }

    default Vector3f getColor() {
        Vector3f v = getColorNamed();
        if (v!=null) return v;
        return particleColorFromInt(this.getColorRaw());
    }
    default int getColorRaw() {
        return 0;
    }
    default float getAlpha() {
        return 1.0f;
    }
}