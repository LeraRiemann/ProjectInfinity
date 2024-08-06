package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DimensionalCreeper extends CreeperEntity implements TintableEntity {
    public static TrackedData<Integer> color = DataTracker.registerData(DimensionalCreeper.class, TrackedDataHandlerRegistry.INTEGER);
    public static TrackedData<Float> range = DataTracker.registerData(DimensionalCreeper.class, TrackedDataHandlerRegistry.FLOAT);
    public static TrackedData<String> biome = DataTracker.registerData(DimensionalCreeper.class, TrackedDataHandlerRegistry.STRING);
    public Registry<Biome> reg;

    public DimensionalCreeper(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }
    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        MinecraftServer s = world.toServerWorld().getServer();
        reg = s.getRegistryManager().get(RegistryKeys.BIOME);
        String biomename = ((MinecraftServerAccess)(s)).getDimensionProvider().registry.get("biomes").getElement(world.getRandom().nextDouble());
        Biome b = reg.get(new Identifier(biomename));
        this.setColor(b != null ? b.getFoliageColor() : 7842607);
        this.setRange(8 + random.nextFloat()*24);
        this.setBiome(biomename);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(color, 7842607);
        this.dataTracker.startTracking(range, 16.0f);
        this.dataTracker.startTracking(biome, "minecraft:plains");
    }
    public void setBiome(String s) {
        this.dataTracker.set(biome, s);
    }
    public Biome getBiome() {
        return reg.get(new Identifier(getBiomeId()));
    }
    public String getBiomeId() {
        return this.dataTracker.get(biome);
    }
    public void setColor(int c) {
        this.dataTracker.set(color, c);
    }

    @Override
    public int getAge() {
        return age;
    }
    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public Text getName() {
        return super.getName();
    }

    @Override
    public int getColorRaw() {
        return this.dataTracker.get(color);
    }
    public void setRange(float s) {
        this.dataTracker.set(range, s);
    }
    public float getRange() {
        return this.dataTracker.get(range);
    }
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("range", this.getRange());
        nbt.putInt("color", this.getColorRaw());
        nbt.putString("biome", this.getBiomeId());
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setRange(nbt.getFloat("range"));
        this.setColor(nbt.getInt("color"));
        this.setBiome(nbt.getString("biome"));
    }

    public void blow_up() {
        float f = 3 * this.getRange() / 16;
        this.dead = true;
        int r = (int)(this.getRange());
        MinecraftServer s = this.getServer();
        if (s != null) {
            ServerWorld serverWorld = s.getWorld(this.getWorld().getRegistryKey());
            if (serverWorld != null) {
                reg = s.getRegistryManager().get(RegistryKeys.BIOME);
                BlockBox blockBox = new BlockBox(getBlockX() - r, getBlockY() - r, getBlockZ() - r,
                        getBlockX() + r, getBlockY() + r, getBlockZ() + r);
                ArrayList<Chunk> list = new ArrayList<>();
                for (int k = ChunkSectionPos.getSectionCoord(blockBox.getMinZ()); k <= ChunkSectionPos.getSectionCoord(blockBox.getMaxZ()); ++k) {
                    for (int l = ChunkSectionPos.getSectionCoord(blockBox.getMinX()); l <= ChunkSectionPos.getSectionCoord(blockBox.getMaxX()); ++l) {
                        Chunk chunk = serverWorld.getChunk(l, k, ChunkStatus.FULL, false);
                        if (chunk != null) {
                            list.add(chunk);
                        }
                    }
                }
                for (Chunk chunk : list) {
                    chunk.populateBiomes(createBiomeSupplier(chunk, blockBox, reg.getEntry(this.getBiome())),
                            serverWorld.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
                    chunk.setNeedsSaving(true);
                }
                serverWorld.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(list);
            }
        }
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), f, World.ExplosionSourceType.NONE);
        this.discard();
    }

    private static BiomeSupplier createBiomeSupplier(Chunk chunk, BlockBox box, RegistryEntry<Biome> biome) {
        return (x, y, z, noise) -> {
            int i = BiomeCoords.toBlock(x);
            int j = BiomeCoords.toBlock(y);
            int k = BiomeCoords.toBlock(z);
            RegistryEntry<Biome> registryEntry2 = chunk.getBiomeForNoiseGen(x, y, z);
            if (box.contains(i, j, k)) {
                return biome;
            }
            return registryEntry2;
        };
    }
}
