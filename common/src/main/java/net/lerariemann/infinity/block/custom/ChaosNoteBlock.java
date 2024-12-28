package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

public class ChaosNoteBlock extends NoteBlock {
    public ChaosNoteBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(115) == 0) {
            this.playNote(world, pos);
        }
    }

    private void playNote(World world, BlockPos pos) {
        world.addSyncedBlockEvent(pos, this, 0, 0);
        world.emitGameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        NoteBlockInstrument[] instruments = NoteBlockInstrument.values();
        NoteBlockInstrument noteBlockInstrument = instruments[world.random.nextInt(instruments.length-7)];
        float f;
        if (noteBlockInstrument.canBePitched()) {
            int i = world.random.nextInt(12);
            f = getNotePitch(i);
            world.addParticle(ParticleTypes.NOTE, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.2, (double)pos.getZ() + (double)0.5F, (double)i / (double)24.0F, (double)0.0F, (double)0.0F);
        } else {
            f = 1.0F;
        }

        world.playSound(null, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, noteBlockInstrument.getSound(), SoundCategory.RECORDS, 3.0F, f, world.random.nextLong());
        return true;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return Items.NOTE_BLOCK.getDefaultStack();
    }
}
