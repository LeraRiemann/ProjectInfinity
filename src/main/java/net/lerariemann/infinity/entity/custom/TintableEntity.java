package net.lerariemann.infinity.entity.custom;

import org.joml.Vector3f;

public interface TintableEntity {
    default Vector3f colorFromInt(int i) {
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        return new Vector3f(f, g, h);
    }
    default Vector3f getColor() {
        return colorFromInt(this.getColorRaw());
    }
    default int getColorRaw() {
        return 0;
    }
    default float getAlpha() {
        return 1.0f;
    }
}
