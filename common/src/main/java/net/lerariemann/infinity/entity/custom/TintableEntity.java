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

    default Vector3f colorFromInt(int i) {
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        return new Vector3f(f, g, h);
    }
    default int getColorNamed() {
        if (hasCustomName()) {
            String s = getName().getString();
            if ("jeb_".equals(s)) {
                int n = getAge() / 25 + getId();
                int o = DyeColor.values().length;
                int p = n % o;
                int q = (n + 1) % o;
                float r = (getAge() % 25) / 25.0f;
                int fs = SheepEntity.getRgbColor(DyeColor.byId(p));
                int gs = SheepEntity.getRgbColor(DyeColor.byId(q));
                float f = fs * (1.0f - r) + gs * r;
                return (int)f;
            }
            if ("hue".equals(s)) {
                int n = getAge() + getId();
                float hue = n / 400.f;
                hue = hue - (int) hue;
                return Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();
            }
        }
        return -1;
    }
    default int getColor() {
        int v = getColorNamed();
        if (v!=-1) return v;
        return this.getColorRaw();
    }

    default int getColorRaw() {
        return 0;
    }
}
