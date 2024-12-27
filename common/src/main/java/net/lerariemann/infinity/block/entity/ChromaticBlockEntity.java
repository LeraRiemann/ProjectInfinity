package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChromaticBlockEntity extends TintableBlockEntity {
    public short hue;
    public short saturation;
    public short brightness;
    public ChromaticBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHROMATIC.get(), pos, state);
        hue = 0;
        saturation = 0;
        brightness = 255;
    }

    public void setColor(int colorHex) {
        setColor(colorHex, null);
    }

    public void setColor(int colorHex, @Nullable AtomicBoolean cancel) {
        if (cancel != null && colorHex == getTint()) cancel.set(true);
        Color c = (new Color(colorHex));
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hue = (short)(hsb[0] * 360);
        saturation = (short)(hsb[1] * 255);
        brightness = (short)(hsb[2] * 255);
        sync();
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(ModItemFunctions.COLOR.get(), getTint());
    }
    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        setColor(components.getOrDefault(ModItemFunctions.COLOR.get(), 0xFFFFFF));
    }

    public static ComponentMap asMap(int i) {
        return ComponentMap.builder()
                .add(ModItemFunctions.COLOR.get(), i)
                .build();
    }
    public ComponentMap asMap() {
        return asMap(getTint());
    }

    public int offsetSaturation(short amount, @Nullable AtomicBoolean cancel) {
        if (cancel != null) cancel.set(amount < 0 ? saturation == 0 : saturation == 255);
        saturation += amount;
        if (saturation < 0) saturation = 0;
        else if (saturation > 255) saturation = 255;
        return saturation;
    }
    public int offsetBrightness(short amount, @Nullable AtomicBoolean cancel) {
        if (cancel != null) cancel.set(amount < 0 ? brightness == 0 : brightness == 255);
        brightness += amount;
        if (brightness < 0) brightness = 0;
        else if (brightness > 255) brightness = 255;
        return brightness;
    }
    public boolean onUse(World world, BlockPos pos, ItemStack stack) {
        SoundEvent event = SoundEvents.BLOCK_NOTE_BLOCK_GUITAR.value();
        float pitch = -1f;
        float volume = 1f;
        AtomicBoolean cancel = new AtomicBoolean(false);
        if (stack.isOf(ModItems.IRIDESCENT_STAR.get())) {
            pitch = offsetSaturation((short) 16, cancel) / 255f;
        }
        else if (stack.isOf(ModItems.STAR_OF_LANG.get())) {
            pitch = offsetSaturation((short) -16, cancel) / 255f;
        }
        else if (stack.isOf(ModItems.WHITE_MATTER.get())) {
            pitch = offsetBrightness((short) 16, cancel) / 255f;
        }
        else if (stack.isOf(ModItems.BLACK_MATTER.get())) {
            pitch = offsetBrightness((short) -16, cancel) / 255f;
        }
        else if (stack.isOf(Items.AMETHYST_SHARD)) {
            hue += 15;
            hue %= 360;
            event = SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE;
        }
        else if (stack.getItem() instanceof DyeItem dye) {
            setColor(dye.getColor().getEntityColor());
            event = SoundEvents.ITEM_DYE_USE;
        }
        else return false;
        if (cancel.get()) return false; //block was already at extremal saturation / brightness, no need for side effects
        pitch = pitch < 0 ? 1f : 0.5f + 1.5f * pitch; //scaling for Minecraft's sound system
        if (!world.isClient()) world.playSound(null, pos, event, SoundCategory.BLOCKS, volume, pitch);
        sync();
        return true;
    }

    void sync() {
        markDirty();
        if (world != null) {
            BlockState bs = world.getBlockState(pos);
            world.updateListeners(pos, bs, bs, 0);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        setColor(nbt.getInt("color"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("color", getTint());
    }

    public int getTint() {
        return Color.HSBtoRGB(hue / 360f, saturation / 255f, brightness / 255f);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
