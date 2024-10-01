package net.lerariemann.infinity.options;

import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

    public float getCelestialTilt() {
        return data.contains("celestial_tilt") ? data.getFloat("celestial_tilt") : -90.0f;
    }

    public float getSolarTilt() {
        return data.contains("solar_tilt") ? data.getFloat("solar_tilt") : -90.0f;
    }

    public Vector3f getSolarTint() {
        int color = data.contains("solar_tint") ? data.getInt("solar_tint") : 16777215;
        return new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }

    public Identifier getSolarTexture() {
        return Identifier.of(data.contains("solar_texture") ? data.getString("solar_texture") : "textures/environment/sun.png");
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

    public int getNumMoons() {
        return data.contains("moons") ? data.getList("moons", NbtElement.COMPOUND_TYPE).size() : 1;
    }
    public boolean lunarTest(String key, int i) {
        return data.contains("moons") && ((NbtCompound)(data.getList("moons", NbtElement.COMPOUND_TYPE).get(i))).contains(key);
    }
    public float fullLunarTest(String key, int i, float def) {
        return lunarTest(key, i) ? ((NbtCompound)(data.getList("moons", NbtElement.COMPOUND_TYPE).get(i))).getFloat(key) : def;
    }

    public float getLunarSize(int i) {
        return fullLunarTest("lunar_size", i, 20.0f);
    }

    public float getLunarTiltY(int i) {
        return fullLunarTest("lunar_tilt_y", i, 0.0f);
    }

    public float getLunarTiltZ(int i) {
        return fullLunarTest("lunar_tilt_z", i, 0.0f);
    }

    public Vector3f getLunarTint(int i) {
        int color = lunarTest("lunar_tint", i) ? ((NbtCompound)(data.getList("moons", NbtElement.COMPOUND_TYPE).get(i))).getInt("lunar_tint") : 16777215;
        return new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }

    public Identifier getLunarTexture(int i) {
        return Identifier.of(lunarTest("lunar_texture", i) ?
                ((NbtCompound)(data.getList("moons", NbtElement.COMPOUND_TYPE).get(i))).getString("lunar_texture") : "textures/environment/moon_phases.png");
    }

    public float getLunarVelocity(int i) {
        return fullLunarTest("lunar_velocity", i, 1.0f);
    }

    public float getLunarOffset(int i) {
        return fullLunarTest("lunar_offset", i, 0.0f);
    }
}
