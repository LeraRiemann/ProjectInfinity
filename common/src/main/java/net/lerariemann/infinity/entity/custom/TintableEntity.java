package net.lerariemann.infinity.entity.custom;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3f;

import java.awt.*;

public interface TintableEntity {
    int getId();


    static Vector3f colorFromInt(int i) {
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        return new Vector3f(f, g, h);
    }

    static Vector3f getColorJeb(int age, int id) {
        int n = age / 25 + id   ;
        int o = DyeColor.values().length;
        int p = n % o;
        int q = (n + 1) % o;
        float r = (age % 25) / 25.0f;
        float[] fs = SheepEntity.getRgbColor(DyeColor.byId(p));
        float[] gs = SheepEntity.getRgbColor(DyeColor.byId(q));
        float f = fs[0] * (1.0f - r) + gs[0] * r;
        float g = fs[1] * (1.0f - r) + gs[1] * r;
        float h = fs[2] * (1.0f - r) + gs[2] * r;
        return new Vector3f(f, g, h);
    }

    Vector3f getColorNamed();

    static Vector3f getColorNamed(String name, int age, int id) {
        if ("jeb_".equals(name)) {
            return TintableEntity.getColorJeb(age, id);
        }
        if ("hue".equals(name)) {
            int n = age + id;
            float hue = n / 400.f;
            hue = hue - (int) hue;
            return colorFromInt(Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
        }
        return null;
    }

    default int getColorRaw() {
        return 0;
    }
}