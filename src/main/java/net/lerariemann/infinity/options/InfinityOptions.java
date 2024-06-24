package net.lerariemann.infinity.options;

import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.io.File;

public record InfinityOptions(NbtCompound data) {
    public static InfinityOptions empty() {
        return new InfinityOptions(new NbtCompound());
    }
    public static InfinityOptions generate(MinecraftServer server, RegistryKey<World> worldKey) {
        String s = worldKey.getValue().toString();
        if (s.contains("infinity:")) {
            String name = s.substring(s.lastIndexOf("infinity:") + 9);
            File f = server.getSavePath(WorldSavePath.DATAPACKS).resolve(name + "/data/infinity/options.json").toFile();
            if (f.exists()) {
                return new InfinityOptions(CommonIO.read(f));
            }
        }
        return empty();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public NbtCompound getShader() {
        return data.contains("shader") ? data.getCompound("shader") : new NbtCompound();
    }

    public String getSkyType() {
        return data.contains("sky_type") ? data.getString("sky_type") : "empty";
    }

    public float getSolarSize() {
        return data.contains("solar_size") ? data.getFloat("solar_size") : 30.0f;
    }

    public float getLunarSize() {
        return data.contains("lunar_size") ? data.getFloat("lunar_size") : 20.0f;
    }

    public float getCelestialTilt() {
        return data.contains("celestial_tilt") ? data.getFloat("celestial_tilt") : -90.0f;
    }

    public float getSolarTilt() {
        return data.contains("solar_tilt") ? data.getFloat("solar_tilt") : -90.0f;
    }

    public Identifier getSolarTexture() {
        return new Identifier(data.contains("solar_texture") ? data.getString("solar_texture") : "textures/environment/sun.png");
    }

    public Identifier getLunarTexture() {
        return new Identifier(data.contains("lunar_texture") ? data.getString("lunar_texture") : "textures/environment/moon_phases.png");
    }

    public Vector3f getStellarColor() {
        int color = data.contains("stellar_color") ? data.getInt("stellar_color") : 16777215;
        return new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }

    public boolean isMoonCustom() {
        return data.contains("lunar_texture");
    }

    public float getCelestialTilesAmount() {
        return data.contains("celestial_tiles_amount") ? data.getFloat("celestial_tiles_amount") : 1.0f;
    }

    public float getCelestialNightBrightness() {
        return data.contains("celestial_night_brightness") ? data.getFloat("celestial_night_brightness") : 0.0f;
    }

    public int getCelestialBrightness() {
        return data.contains("celestial_brightness") ? data.getInt("celestial_brightness") : 255;
    }

    public int getCelestialAlpha() {
        return data.contains("celestial_alpha") ? data.getInt("celestial_alpha") : 255;
    }

    public float getCelestialVelocity() {
        return data.contains("celestial_velocity") ? data.getFloat("celestial_velocity") : 0.0f;
    }

    public boolean endSkyLike() {
        return data.contains("end_sky_like") && data.getBoolean("end_sky_like");
    }

    public int getNumStars() {
        return data.contains("num_stars") ? data.getInt("num_stars") : 1500;
    }
    public float getStarSizeBase() {
        return data.contains("star_size_base") ? data.getFloat("star_size_base") : 0.15f;
    }
    public float getStarSizeModifier() {
        return data.contains("star_size_modifier") ? data.getFloat("star_size_modifier") : 0.1f;
    }

    public double getTimeScale() {
        return data.contains("time_scale") ? data.getDouble("time_scale") : 1.0;
    }

    public double getMavity() {
        return data.contains("mavity") ? data.getDouble("mavity") : 1.0;
    }
}
