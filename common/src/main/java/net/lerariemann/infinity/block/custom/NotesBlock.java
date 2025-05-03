package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class NotesBlock extends Block {
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty TICKING = BooleanProperty.of("ticking");
    public NotesBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(POWERED, false).with(TICKING, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
        builder.add(TICKING);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.contains(TICKING) && state.get(TICKING);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        boolean bl = world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            if (bl) this.play(null, world, pos);
            world.setBlockState(pos, state.with(POWERED, bl), 3);
        }
    }

    public boolean isTicking(ServerWorld world, BlockState state) {
        return state.get(TICKING) && (!InfinityOptions.access(world).isHaunted());
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (isTicking(world, state) && random.nextInt(115) == 0) {
            this.play(null, world, pos);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            boolean bl = state.get(TICKING);
            world.setBlockState(pos, state.with(TICKING, !bl), 3);
            play(player, world, pos);
            player.incrementStat(Stats.TUNE_NOTEBLOCK);
            return ActionResult.CONSUME;
        }
    }

    @Override
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            play(player, world, pos);
            player.incrementStat(Stats.PLAY_NOTEBLOCK);
        }
    }

    public void play(@Nullable Entity entity, World world, BlockPos pos) {
        if (world.getBlockState(pos.up()).isAir()) {
            world.addSyncedBlockEvent(pos, this, 0, 0);
            world.emitGameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, pos);
        }
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        NoteBlockInstrument noteBlockInstrument;
        float f;
        if (world.getBlockEntity(pos.down()) instanceof ChromaticBlockEntity e) {
            noteBlockInstrument = NoteBlockInstrument.GUITAR;
            f = (float)Math.pow(2.0, 2 * (e.brightness / 255f) - 1);
        }
        else {
            NoteBlockInstrument[] instruments = NoteBlockInstrument.values();
            noteBlockInstrument = instruments[world.random.nextInt(instruments.length-7)];
            if (noteBlockInstrument.canBePitched()) {
                int i = world.random.nextInt(24);
                f = NoteBlock.getNotePitch(i);
                world.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, i / 24.0, 0.0F, 0.0F);
            } else {
                f = 1.0F;
            }
        }

        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, noteBlockInstrument.getSound(), SoundCategory.RECORDS, 3.0F, f, world.random.nextLong());
        return true;
    }
}
