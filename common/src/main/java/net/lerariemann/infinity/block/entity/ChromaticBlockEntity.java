package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
    public int color;
    public ChromaticBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHROMATIC.get(), pos, state);
        hue = 0;
        saturation = 0;
        brightness = 255;
    }

    public void setColor(int colorHex) {
        setColor(colorHex, null);
    }

    public void setColor(int hue, int saturation, int brightness, @Nullable AtomicBoolean cancel) {
        if (cancel != null && this.hue == hue && this.saturation == saturation && this.brightness == brightness) {
            cancel.set(true);
            return;
        }
        this.hue = (short)hue;
        this.saturation = (short)saturation;
        this.brightness = (short)brightness;
        updateColor();
        sync();
    }

    public void setColor(int colorHex, @Nullable AtomicBoolean cancel) {
        if (cancel != null && colorHex == color) {
            cancel.set(true);
            return;
        }
        Color c = (new Color(colorHex));
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hue = (short)(hsb[0] * 360);
        saturation = (short)(hsb[1] * 255);
        brightness = (short)(hsb[2] * 255);
        color = colorHex;
        sync();
    }
    public void updateColor() {
        color = Color.HSBtoRGB(hue / 360f, saturation / 255f, brightness / 255f) & 0xFFFFFF;
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(ModComponentTypes.COLOR.get(), color);
    }
    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        setColor(components.getOrDefault(ModComponentTypes.COLOR.get(), 0xFFFFFF));
    }

    public static ComponentMap asMap(int i) {
        return ComponentMap.builder()
                .add(ModComponentTypes.COLOR.get(), i)
                .build();
    }
    public ComponentMap asMap() {
        return asMap(getTint());
    }
    public short offset(short orig, short amount, AtomicBoolean cancel) {
        if (amount < 0 ? orig == 0 : orig == 255) {
            cancel.set(true);
            return orig;
        }
        orig += amount;
        if (orig < 0) orig = 0;
        else if (orig > 255) orig = 255;
        return orig;
    }
    public boolean onUse(World world, BlockPos pos, ItemStack stack) {
        SoundEvent event = SoundEvents.BLOCK_NOTE_BLOCK_GUITAR.value();
        float pitch = -1f;
        float volume = 1f;
        AtomicBoolean cancel = new AtomicBoolean(false);

        if (stack.isOf(Items.AMETHYST_SHARD)) {
            saturation = offset(saturation, (short) 16, cancel);
            pitch = saturation / 255f;
        }
        else if (stack.isOf(ModItems.FOOTPRINT.get())) {
            saturation = offset(saturation, (short) -16, cancel);
            pitch = saturation / 255f;
        }
        else if (stack.isOf(ModItems.WHITE_MATTER.get())) {
            brightness = offset(brightness, (short) 16, cancel);
            pitch = brightness / 255f;
        }
        else if (stack.isOf(ModItems.BLACK_MATTER.get())) {
            brightness = offset(brightness, (short) -16, cancel);
            pitch = brightness / 255f;
        }
        else if (stack.getItem() instanceof DyeItem dye) {
            setColor(ColorLogic.getChromaticColor(dye.getColor()), cancel);
            event = SoundEvents.ITEM_DYE_USE;
        }
        else return false;
        if (cancel.get()) return false; //block was already at extremal saturation / brightness, no need for side effects
        updateColor();
        pitch = pitch < 0 ? 1f : 0.5f + 1.5f * pitch; //scaling for Minecraft's sound system
        if (!world.isClient()) world.playSound(null, pos, event, SoundCategory.BLOCKS, volume, pitch);
        sync();
        return true;
    }

    public void onIridStarUse(boolean reverse) {
        if (reverse) {
            hue -= 10;
            if (hue < 0) hue += 360;
        }
        else {
            hue += 10;
            if (hue > 360) hue -= 360;
        }
        updateColor();
        sync();
    }

    void sync() {
        markDirty();
        if (world != null) {
            BlockState bs = world.getBlockState(pos);
            bs.updateNeighbors(world, pos, 3);
            world.updateListeners(pos, bs, bs, 0);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("color", NbtElement.INT_TYPE))
            setColor(nbt.getInt("color"));
        else if (nbt.contains("color", NbtElement.COMPOUND_TYPE)) {
            NbtCompound color = nbt.getCompound("color");
            hue = color.getShort("h");
            saturation = color.getShort("s");
            brightness = color.getShort("b");
            updateColor();
        }
    }
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        NbtCompound color = new NbtCompound();
        color.putShort("h", hue);
        color.putShort("s", saturation);
        color.putShort("b", brightness);
        nbt.put("color", color);
    }

    public int getTint() {
        return color;
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
