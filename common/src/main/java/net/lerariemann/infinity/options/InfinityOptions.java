package net.lerariemann.infinity.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.io.File;
import java.util.function.Function;
import static net.lerariemann.infinity.util.core.NbtUtils.*;

public class InfinityOptions {
    public NbtCompound data;
    public PitchShifter shifter;
    public EffectGiver effect;
    public IridescentMap iridMap;

    public InfinityOptions(NbtCompound data) {
        this.data = data;
        this.shifter = PitchShifter.decode(test(data, "pitch_shift", new NbtCompound()));
        this.effect = EffectGiver.of(test(data, "effect", new NbtCompound()));
        this.iridMap = IridescentMap.decode(test(data, "iridescent_map", new NbtCompound()));
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

    public static InfinityOptions access(World world) {
        return ((InfinityOptionsAccess)world).infinity$getOptions();
    }
    @Environment(EnvType.CLIENT)
    public static InfinityOptions ofClient() {
        return ofClient(MinecraftClient.getInstance());
    }
    @Environment(EnvType.CLIENT)
    public static InfinityOptions ofClient(MinecraftClient client) {
        return ((InfinityOptionsAccess)client).infinity$getOptions();
    }
    public static InfinityOptions nullSafe(InfinityOptions options) {
        return (options != null) ? options : InfinityOptions.empty();
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
    public boolean endSkyLike() {
        return test(data, "end_sky_like", false);
    }
    public boolean hasDawn() {
        return test(data, "dawn", !getSkyType().equals("rainbow"));
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
        return Identifier.of(test(data, "solar_texture", "textures/environment/sun.png"));
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
    public float getDayStarBrightness() {
        return test(data, "star_brightness_day", 0.0f);
    }
    public float getNightStarBrightness() {
        return test(data, "star_brightness_night", 0.5f);
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
