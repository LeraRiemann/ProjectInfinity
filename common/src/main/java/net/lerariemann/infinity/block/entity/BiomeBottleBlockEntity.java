package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class BiomeBottleBlockEntity extends BlockEntity {
    private final PropertyDelegate propertyDelegate;
    private Identifier biome;
    private int color;
    public int charge;
    private int from_charge;

    public BiomeBottleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIOME_BOTTLE.get(), pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                if (index == 0) {
                    return BiomeBottleBlockEntity.this.color;
                }
                return 0;
            }

            public void set(int index, int value) {
                if (index == 0) {
                    BiomeBottleBlockEntity.this.color = value;
                }

            }
            public int size() {
                return 1;
            }
        };
        this.empty();
    }

    public void setBiome(RegistryEntry<Biome> biome) {
        if (biome.getKey().isPresent()) setBiome(biome.value().getSkyColor(), biome.getKey().get().getValue());
    }

    public void setBiome(int c, Identifier i) {
        this.color = c;
        this.biome = i;
    }

    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(tag, registryLookup);
        tag.putString("Biome", biome.toString());
        tag.putInt("Color", color);
        tag.putInt("Charge", charge);
        if (from_charge > 0) tag.putInt("from_charge", from_charge);
    }

    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);
        this.charge = tag.getInt("Charge");
        this.biome = Identifier.of(tag.getString("Biome"));
        this.color = tag.getInt("Color");
        this.from_charge = InfinityOptions.test(tag, "from_charge", 0);
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        BiomeBottle.addComponents(componentMapBuilder, biome, color, charge);
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        this.biome = components.getOrDefault(ModItemFunctions.BIOME_CONTENTS.get(), BiomeBottle.defaultBiome());
        this.color = components.getOrDefault(ModItemFunctions.COLOR.get(), 0xFFFFFF);
        this.charge = components.getOrDefault(ModItemFunctions.CHARGE.get(), 0);
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    public Object getRenderData() {
        return propertyDelegate.get(0);
    }

    public ItemStack asStack() {
        ItemStack itemStack = ModItems.BIOME_BOTTLE_ITEM.get().getDefaultStack();
        itemStack.applyComponentsFrom(this.createComponentMap());
        return itemStack;
    }

    public boolean isTicking() {
        return from_charge > 0;
    }

    public void startTicking() {
        from_charge = charge;
    }

    public void empty() {
        color = 0xFFFFFF;
        biome = BiomeBottle.defaultBiome();
        charge = 0;
        from_charge = 0;
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BiomeBottleBlockEntity be) {
        if (be.isTicking() && world instanceof ServerWorld w) {
            if (w.getTime() % 20 == 0) {
                int level = be.charge/100;
                if (level <= 0) {
                    be.empty();
                }
                else {
                    int diff2 = be.charge > 1500 ? 500 : 100;
                    int diff = be.charge%diff2;
                    if (diff == 0) diff = diff2;
                    int charge_new = be.charge - diff;
                    BiomeBottle.spreadRing(w, pos, be.biome, be.from_charge - be.charge, be.from_charge - charge_new);
                    be.charge = charge_new;
                    world.setBlockState(pos, state.with(BiomeBottle.LEVEL, Math.clamp(level - 1, 0, 10)));
                    BiomeBottle.playSploosh(w, pos);
                    be.markDirty();
                }
            }
        }
    }
}
