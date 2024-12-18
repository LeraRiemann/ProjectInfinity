package net.lerariemann.infinity.block.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.item.PortalDataHolder;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.InfinityPortal;
import net.lerariemann.infinity.util.PortalCreator;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class InfinityPortalBlock extends NetherPortalBlock implements BlockEntityProvider {
    public static final BooleanProperty BOOP = BooleanProperty.of("boop");

    public InfinityPortalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(BOOP, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BOOP);
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new InfinityPortalBlockEntity(pos, state, World.OVERWORLD.getValue());
    }

    /**
     * This is being called when the portal is right-clicked.
     */
    @Override
    public ActionResult onUse(BlockState state, World w, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (w instanceof ServerWorld world) {
            MinecraftServer s = world.getServer();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
                /* If the portal is open already, nothing should happen. */
                if (npbe.isOpen() && world_exists(s, npbe.getDimension()))
                    return ActionResult.PASS;

                /* If the portal key is blank, open the portal on any right-click. */
                RandomProvider prov = InfinityMod.provider;
                Optional<Item> key = prov.getPortalKeyAsItem();
                if (key.isEmpty()) {
                    if (!npbe.isOpen()) PortalCreator.openWithStatIncrease(player, s, world, pos);
                }

                /* Otherwise check if we're using the correct key. If so, open. */
                else {
                    ItemStack usedKey = player.getStackInHand(Hand.MAIN_HAND);
                    if (usedKey.isOf(key.get())) {
                        if (!player.getAbilities().creativeMode && prov.rule("consumePortalKey")) {
                            usedKey.decrement(1); // Consume the key if needed
                        }
                        PortalCreator.openWithStatIncrease(player, s, world, pos);
                    }
                }
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe)
            return ModItems.TRANSFINITE_KEY.get().withPortalData(ipbe);
        return ItemStack.EMPTY;
    }

    static boolean world_exists(MinecraftServer s, Identifier l) {
        return (!l.getNamespace().equals(InfinityMod.MOD_ID)) ||
                s.getSavePath(WorldSavePath.DATAPACKS).resolve(l.getPath()).toFile().exists() ||
                s.getWorldRegistryKeys().contains(RegistryKey.of(RegistryKeys.WORLD, l));
    }

    /**
     * Spawns colourful particles.
     */
    @Environment(EnvType.CLIENT) @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (random.nextInt(100) == 0) {
            world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for(int i = 0; i < 4; ++i) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = ((double)random.nextFloat() - 0.5) * 0.5;
            double h = ((double)random.nextFloat() - 0.5) * 0.5;
            double j = ((double)random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;
            if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
                d = (double)pos.getX() + 0.5 + 0.25 * (double)k;
                g = random.nextFloat() * 2.0F * (float)k;
            } else {
                f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
                j = random.nextFloat() * 2.0F * (float)k;
            }

            ParticleEffect eff = ParticleTypes.PORTAL;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
                int colorInt = npbe.getPortalColor();
                Vec3d vec3d = Vec3d.unpackRgb(colorInt);
                double color = 1.0D + (colorInt >> 16 & 0xFF) / 255.0D;
                eff = new DustParticleEffect(new Vector3f((float)vec3d.x, (float)vec3d.y, (float)vec3d.z), (float)color);
            }

            world.addParticle(eff, d, e, f, g, h, j);
        }
    }

    /**
     * Adds logic for portal-based recipes.
     */
    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        AtomicBoolean bl = new AtomicBoolean(false);
        if (w instanceof ServerWorld world
                && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
            MinecraftServer server = world.getServer();
            if (entity instanceof ItemEntity e) {
                ModItemFunctions.checkCollisionRecipes(world, e, ModItemFunctions.PORTAL_CRAFTING_TYPE.get(),
                        item -> PortalDataHolder.addPortalComponents(item, e.getStack(), ipbe));
                InfinityMod.provider.getPortalKeyAsItem().ifPresent(item -> { //opening a portal by tossing a key in
                    if (e.getStack().isOf(item)) {
                        InfinityPortal.tryUpdateOpenStatus(ipbe, world, pos, server);
                        if (ipbe.isOpen()) return;
                        PlayerEntity nearestPlayer =
                                world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);
                        PortalCreator.openWithStatIncrease(nearestPlayer, server, world, pos);
                        e.getStack().decrement(1);
                        e.setVelocity(e.getVelocity().multiply(-1));
                        e.setPortalCooldown(200);
                        bl.set(true);
                    }
                });
            }
            if (entity instanceof PlayerEntity player
                    && InfinityMod.provider.isPortalKeyBlank()) {
                ServerWorld world1 = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, ipbe.getDimension()));
                if ((world1 == null) || !ipbe.isOpen())
                    PortalCreator.openWithStatIncrease(player, server, world, pos);
                else {
                    Timebombable tw = (Timebombable)world1;
                    if (tw.infinity$isTimebombed() && tw.infinity$tryRestore()) {
                        new RandomDimension(ipbe.getDimension(), server);
                        PortalCreator.openWithStatIncrease(player, server, world, pos);
                    }
                }
            }
        }
        if (!bl.get()) super.onEntityCollision(state, w, pos, entity);
    }
    /**
     * Spawns chaos pawns in the portal.
     */
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
                && random.nextInt(2000) < world.getDifficulty().getId()) {
            ChaosPawn entity;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, ModEntities.CHAOS_PAWN.get()) &&
                    InfinityMethods.chaosMobsEnabled() &&
                    (entity = ModEntities.CHAOS_PAWN.get().spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                entity.resetPortalCooldown();
                BlockEntity blockEntity = world.getBlockEntity(pos.up());
                if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
                    int color = npbe.getPortalColor();
                    Vec3d c = Vec3d.unpackRgb(color);
                    entity.setAllColors((int)(256 * c.z) + 256 * (int)(256 * c.y) + 65536 * (int)(256 * c.x));
                }
            }
        }
    }

    /**
     * This is called when something's trying to use the portal and returns the data on where they end up.
     */
    @Nullable @Override
    public TeleportTarget createTeleportTarget(ServerWorld worldFrom, Entity entity, BlockPos posFrom) {
        if (worldFrom.getBlockEntity(posFrom) instanceof InfinityPortalBlockEntity ipbe) {
            return (new InfinityPortal(ipbe, worldFrom, posFrom)).getTeleportTarget(entity);
        }
        else if (entity instanceof ServerPlayerEntity player)
            InfinityMethods.sendUnexpectedError(player, "portal");
        return InfinityPortal.emptyTarget(entity); //if anything goes wrong, don't teleport anywhere
    }
}
