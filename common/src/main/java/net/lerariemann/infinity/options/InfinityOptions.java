package net.lerariemann.infinity.options;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.joml.Vector3f;

import java.io.File;
import java.util.function.Function;

public class InfinityOptions {
    public NbtCompound data;
    public PitchShifter shifter;
    public EffectGiver effect;

    public InfinityOptions(NbtCompound data) {
        this.data = data;
        this.shifter = data.contains("pitch_shift") ? new PitchShifter(data.getCompound("pitch_shift")) : new PitchShifter();
        this.effect = data.contains("effect") ? EffectGiver.of(data.getCompound("effect")) : EffectGiver.empty();
    }

    public static PortalColorApplier extractApplier(NbtCompound data) {
        if (!data.contains("portal_color")) return new PortalColorApplier.Empty();
        if (data.contains("portal_color", NbtElement.INT_TYPE)) return new PortalColorApplier.Simple(data.getInt("portal_color"));
        NbtCompound applierData = data.getCompound("portal_color");
        return switch (applierData.getString("type")) {
            case "simple" -> new PortalColorApplier.Simple(applierData.getInt("value"));
            case "checker" -> new PortalColorApplier.Checker(applierData.getList("values", NbtElement.INT_TYPE));
            case "random_hue" -> new PortalColorApplier.RandomHue(applierData);
            case "random" -> new PortalColorApplier.Random();
            default -> new PortalColorApplier.Empty();
        };
    }

    public NbtCompound data() {
        return data;
    }

    public static InfinityOptions empty() {
        return new InfinityOptions(new NbtCompound());
    }

    public static NbtCompound readData(MinecraftServer server, Identifier worldId) {
        if (worldId.getNamespace().equals(InfinityMod.MOD_ID)) {
            String name = worldId.getPath();
            File f = server.getSavePath(WorldSavePath.DATAPACKS).resolve(name + "/data/infinity/options.json").toFile();
            if (f.exists()) {
                return CommonIO.read(f);
            }
        }
        return new NbtCompound();
    }
    public static InfinityOptions generate(MinecraftServer server, Identifier worldId) {
        return new InfinityOptions(readData(server, worldId));
    }

    public static String test(NbtCompound data, String key, String def) {
        return data.contains(key, NbtElement.STRING_TYPE) ? data.getString(key) : def;
    }
    public static NbtCompound test(NbtCompound data, String key, NbtCompound def) {
        return data.contains(key, NbtElement.COMPOUND_TYPE) ? data.getCompound(key) : def;
    }
    public static float test(NbtCompound data, String key, float def) {
        return data.contains(key, NbtElement.DOUBLE_TYPE) ? data.getFloat(key) : def;
    }
    public static int test(NbtCompound data, String key, int def) {
        return data.contains(key, NbtElement.INT_TYPE) ? data.getInt(key) : def;
    }
    public static double test(NbtCompound data, String key, double def) {
        return data.contains(key, NbtElement.DOUBLE_TYPE) ? data.getDouble(key) : def;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public NbtCompound getShader() {
        return test(data, "shader", new NbtCompound());
    }

    public double getTimeScale() {
        return test(data, "time_scale", 1.0);
    }
    public double getMavity() {
        return test(data, "mavity", 1.0);
    }
    public Function<Float, Float> getSoundPitch() {
        return shifter.applier();
    }

    //sky - common
    public String getSkyType() {
        return test(data, "sky_type", "empty");
    }
    public float getHorizonShadingRatio() {
        return test(data, "horizon_shading_ratio", 1.0f);
    }

    //sun
    public float getSolarSize() {
        return test(data, "solar_size", 30.0f);
    }
    public float getSolarTilt() {
        return test(data, "solar_tilt", -90.0f);
    }
    public Vector3f getSolarTint() {
        int color = test(data, "solar_tint",16777215);
        return new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }
    public Identifier getSolarTexture() {
        return new Identifier(test(data, "solar_texture", "textures/environment/sun.png"));
    }

    //stars
    public int getNumStars() {
        return test(data, "num_stars", 1500);
    }
    public float getStarSizeBase() {
        return test(data, "star_size_base", 0.15f);
    }
    public float getStarSizeModifier() {
        return test(data, "star_size_modifier", 0.1f);
    }
    public float getStellarTiltY() {
        return test(data, "stellar_tilt_y", -90.0f);
    }
    public float getStellarTiltZ() {
        return test(data, "stellar_tilt_z", 0.0f);
    }
    public float getStellarVelocity() {
        return test(data, "stellar_velocity", 1.0f);
    }
    public Vector3f getStellarColor() {
        int color = test(data, "stellar_color",16777215);
        return new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }

    //moons
    public boolean isMoonCustom() {
        return data.contains("lunar_texture");
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
        return fullLunarTest("lunar_tilt_y", i, -90.0f);
    }
    public float getLunarTiltZ(int i) {
        return fullLunarTest("lunar_tilt_z", i, 0.0f);
    }
    public Vector3f getLunarTint(int i) {
        int color = lunarTest("lunar_tint", i) ? ((NbtCompound)(data.getList("moons", NbtElement.COMPOUND_TYPE).get(i))).getInt("lunar_tint") : 16777215;
        return new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }
    public Identifier getLunarTexture(int i) {
        return new Identifier(lunarTest("lunar_texture", i) ?
                ((NbtCompound)(data.getList("moons", NbtElement.COMPOUND_TYPE).get(i))).getString("lunar_texture") : "textures/environment/moon_phases.png");
    }
    public float getLunarVelocity(int i) {
        return fullLunarTest("lunar_velocity", i, 1.0f);
    }
    public float getLunarOffset(int i) {
        return fullLunarTest("lunar_offset", i, 0.0f);
    }

    //skybox
    public boolean endSkyLike() {
        return data.contains("end_sky_like") && data.getBoolean("end_sky_like");
    }
}
