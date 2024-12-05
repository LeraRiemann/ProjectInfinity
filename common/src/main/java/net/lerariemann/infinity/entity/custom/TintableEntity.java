package net.lerariemann.infinity.entity.custom;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3f;

public interface TintableEntity {
    int getId();

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
        return ColorHelper.lerp(r, s, t);
    }

    int getColorNamed();

    default int getColorForRender() {
        int v = getColorNamed();
        if (v!=-1) return v;
        return ColorHelper.fullAlpha(this.getColorRaw());
    }

    default int getColorRaw() {
        return 0;
    }
}
