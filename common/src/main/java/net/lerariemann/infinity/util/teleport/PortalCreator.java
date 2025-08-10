package net.lerariemann.infinity.util.teleport;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.custom.Boopable;
import net.lerariemann.infinity.compat.CreateCompat;
import net.lerariemann.infinity.compat.DimLibCompat;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.block.custom.InfinityPortalBlock;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.lerariemann.infinity.util.loading.DimensionGrabber;
import net.lerariemann.infinity.options.PortalColorApplier;
import net.lerariemann.infinity.registry.var.ModCriteria;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.var.ModStats;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.compat.ComputerCraftCompat.checkPrintedPage;

public interface PortalCreator {
    /**
     * Check if the item that is colliding with the Portal can be used to
     * transform it into an Infinity Portal.
     */
    static void tryCreatePortalFromItem(ServerWorld world, BlockPos pos, ItemEntity entity) {
        if (entity.isRemoved()) return;
        ItemStack itemStack = entity.getStack();

        /* Check if the item provided is a transfinite key. */
        if (entity.getStack().getItem().equals(ModItems.TRANSFINITE_KEY.get())) {
            Identifier key_dest = ModItems.TRANSFINITE_KEY.get().getDestinationParsed(itemStack, world);
            boolean bl = modifyOnInitialCollision(key_dest, world, pos);
            if (bl) entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            return;
        }

        /* Check if the item provided is a book of some kind. */
        WritableBookContentComponent writableComponent = itemStack.getComponents().get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        WrittenBookContentComponent writtenComponent = itemStack.getComponents().get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        String printedComponent = null;
        if (Platform.isModLoaded("computercraft")) {
            try {
                printedComponent = checkPrintedPage(itemStack);
            }
            catch (Exception e) {
                InfinityMod.LOGGER.warn("An incompatible version of ComputerCraft is present. Please update it to the latest version");
            }
        }
        if (writableComponent != null || writtenComponent != null || printedComponent != null) {
            String content = parseComponents(writableComponent, writtenComponent, printedComponent);
            content = InfinityMethods.dimTextPreprocess(content);
            Identifier id = InfinityMethods.dimTextToId(content);
            boolean bl = modifyOnInitialCollision(id, world, pos);
            if (bl) entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            recordIdTranslation(world.getServer(), id, content);
        }
    }

    /**
     * Extracts the string used to generate the dimension ID from component content.
     */
    static String parseComponents(WritableBookContentComponent writableComponent, WrittenBookContentComponent writtenComponent, String printedComponent) {
        String content = "";
        try {
            if (writableComponent != null) {
                content = writableComponent.pages().getFirst().raw();
            }
            if (writtenComponent != null) {
                content = writtenComponent.pages().getFirst().raw().getString();
            }
        }
        catch (NoSuchElementException e) {
            content = "";
        }

        if (printedComponent != null) {
            content = printedComponent;
        }
        return content;
    }

    /**
     * Sets the portal color and destination and calls to open the portal immediately if the portal key is blank.
     * Statistics for opening the portal are attributed to the nearest player.
     */
    static boolean modifyOnInitialCollision(Identifier dimName, ServerWorld world, BlockPos pos) {
        MinecraftServer server = world.getServer();
        PlayerEntity nearestPlayer = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);

        if (((MinecraftServerAccess)server).infinity$needsInvocation()) {
            onInvocationNeedDetected(nearestPlayer);
            return false;
        }

        /* Set color and destination. Open status = the world that is being accessed exists already. */
        boolean dimensionExistsAlready = server.getWorldRegistryKeys().contains(RegistryKey.of(RegistryKeys.WORLD, dimName));
        modifyPortalRecursive(world, pos, dimName, dimensionExistsAlready);

        if (dimensionExistsAlready) {
            if (nearestPlayer != null) nearestPlayer.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
            runAfterEffects(world, pos, false, true);
        }

        /* If the portal key is blank, open the portal immediately. */
        else if (InfinityMod.provider.isPortalKeyBlank()) {
            openWithStatIncrease(nearestPlayer, server, world, pos);
        }
        else {
            runAfterEffects(world, pos, false, false);
        }
        return true;
    }

    /* Calls to open the portal and attributes the relevant statistics to a player provided. */
    static void openWithStatIncrease(PlayerEntity player, MinecraftServer s, ServerWorld world, BlockPos pos) {
        if (((MinecraftServerAccess)s).infinity$needsInvocation()) {
            onInvocationNeedDetected(player);
            return;
        }
        boolean isDimensionNew = open(s, world, pos);
        if (player != null) {
            if (isDimensionNew) {
                player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
                ModCriteria.DIMS_OPENED.get().trigger((ServerPlayerEntity)player);
            }
            player.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
        }
    }

    static void onInvocationNeedDetected(PlayerEntity player) {
        if (player != null) player.sendMessage(Text.translatable("error.infinity.invocation_needed"));
    }

    /**
     * Opens the portal by trying to make it usable, including a call to generate a dimension if needed.
     */
    static boolean open(MinecraftServer s, ServerWorld world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        boolean bl = false;
        if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
            /* Call dimension creation. */
            Identifier i = npbe.getDimension();
            if (i.getNamespace().equals(MOD_ID)) {
                bl = tryAddInfinityDimension(s, i);
            }

            /* Set the portal's open status making it usable. */
            modifyPortalRecursive(world, pos, be -> {
                be.setOpen(true);
                be.markDirty();
            });
            runAfterEffects(world, pos, bl, true);
        }
        return bl;
    }

    /**
     * Updates this and all neighbouring portal blocks with a new dimension and open status.
     */
    static void modifyPortalRecursive(ServerWorld world, BlockPos pos, Identifier id, boolean open) {
        modifyPortalRecursive(world, pos, forInitialSetupping(world, pos, id, open));
    }

    /**
     * Recursively creates a queue of neighbouring portal blocks and for each of them executes an action.
     */
    static void modifyPortalRecursive(ServerWorld world, BlockPos pos, Consumer<InfinityPortalBlockEntity> consumer) {
        modifyPortalRecursive(world, pos, new PortalModifier(consumer));
    }
    static void modifyPortalRecursive(ServerWorld world, BlockPos pos, BiConsumer<World, BlockPos> modifier) {
        Direction.Axis axis = world.getBlockState(pos).get(NetherPortalBlock.AXIS);
        BlockLocating.Rectangle rect = BlockLocating.getLargestRectangle(pos, axis, 21,
                Direction.Axis.Y, 21, posx -> world.getBlockState(posx).getBlock() instanceof NetherPortalBlock);
        for (int i = 0; i < rect.width; i++) for (int j = 0; j < rect.height; j++) {
            BlockPos blockPos = rect.lowerLeft.up(j).offset(axis, i);
            modifier.accept(world, blockPos);
        }
    }

    static Consumer<BlockPos> infPortalSetupper(ServerWorld world, BlockPos pos) {
        BlockState originalState = world.getBlockState(pos);
        BlockState state = ModBlocks.PORTAL.get().getDefaultState()
                .with(NetherPortalBlock.AXIS, originalState.get(NetherPortalBlock.AXIS))
                .with(InfinityPortalBlock.BOOP, !Boopable.getBoop(world.getBlockState(pos)));
        return p -> world.setBlockState(p, state, 3, 1);
    }

    static PortalModifierUnion forInitialSetupping(ServerWorld world, BlockPos pos, Identifier id, boolean open) {
        PortalColorApplier applier = PortalColorApplier.of(id, world.getServer());
        PortalModifierUnion union = new PortalModifierUnion()
                .addSetupper(infPortalSetupper(world, pos))
                .addModifier(nbpe -> nbpe.setDimension(id))
                .addModifier(npbe -> npbe.setColor(applier.apply(npbe.getPos())))
                .addModifier(npbe -> npbe.setOpen(open))
                .addModifier(BlockEntity::markDirty);
        if (Platform.isModLoaded("create")) {
            union.addModifier(CreateCompat::tryModifyRails);
        }
        return union;
    }

    /**
     * Calls to create the dimension based on its ID. Returns true if the dimension being opened is indeed brand new.
     */
    static boolean tryAddInfinityDimension(MinecraftServer server, Identifier id) {
        /* checks if the dimension requested is valid and does not already exist */
        if (!id.getNamespace().equals(MOD_ID)) return false;
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        if (((MinecraftServerAccess)(server)).infinity$hasToAdd(key)) return false;
        ServerWorld w = server.getWorld(key);
        if (w!=null) return false;

        /* creates the dimension datapack */
        RandomDimension d = new RandomDimension(id, server);

        // create the dimension
        if (Platform.isModLoaded("dimlib")) {
            DimLibCompat.add(server, id, DimensionGrabber.readDimensionFromDisk(d));
        } else {
            ((MinecraftServerAccess)(server)).infinity$addWorld(key, DimensionGrabber.readDimensionFromDisk(d));
        }
        server.getPlayerManager().getPlayerList().forEach(
                a -> sendNewWorld(a, id, d)); //and send everyone its data for clientside updating
        return true;
    }

    /**
     * Create and send S2C packets necessary for the client to process a freshly added dimension.
     */
    static void sendNewWorld(ServerPlayerEntity player, Identifier id, RandomDimension d) {
        d.random_biomes.forEach(b -> ModPayloads.sendBiomeAddPayload(player, InfinityMethods.getId(b.name), b.data));
        ModPayloads.sendWorldAddPayload(player, id, d.type != null ? d.type.data : new NbtCompound());
    }

    /**
     * Jingle signaling if the portal is usable or not.
     */
    static void runAfterEffects(ServerWorld world, BlockPos pos, boolean dimensionIsNew, boolean portalWorks) {
        if (!portalWorks) playSound(world, pos, SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
        else {
            if (dimensionIsNew) playSound(world, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER);
            playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE);
        }
    }
    static void playSound(ServerWorld world, BlockPos pos, SoundEvent soundEvent) {
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1f, 1f);
    }

    record PortalModifier(Consumer<InfinityPortalBlockEntity> modifier) implements BiConsumer<World, BlockPos> {
        @Override
        public void accept(World world, BlockPos pos) {
            if (world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity npbe) modifier.accept(npbe);
        }
    }

    record PortalModifierUnion(List<Consumer<BlockPos>> setuppers, List<Consumer<InfinityPortalBlockEntity>> modifiers)
            implements BiConsumer<World, BlockPos> {
        public PortalModifierUnion() {
            this(new ArrayList<>(), new ArrayList<>());
        }
        PortalModifierUnion addSetupper(Consumer<BlockPos> setupper) {
            setuppers.add(setupper);
            return this;
        }
        public PortalModifierUnion addModifier(Consumer<InfinityPortalBlockEntity> modifier) {
            modifiers.add(modifier);
            return this;
        }

        @Override
        public void accept(World world, BlockPos pos) {
            setuppers.forEach(setupper -> setupper.accept(pos));
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity npbe)
                modifiers.forEach(modifier -> modifier.accept(npbe));
        }
    }

    static void recordIdTranslation(MinecraftServer server, Identifier id, String value) {
        Path dir = server.getSavePath(WorldSavePath.DATAPACKS).resolve("infinity");
        String filename = "translation_tables.json";
        NbtCompound comp = CommonIO.read(dir.resolve(filename));
        String key = id.getPath();
        if (comp.contains(key)) {
            NbtList l;
            if (comp.getType(key) == NbtElement.STRING_TYPE) {
                l = new NbtList();
                l.add(comp.get(key));
            }
            else l = NbtUtils.getList(comp, key, NbtElement.STRING_TYPE);
            NbtString nbts = NbtString.of(value);
            if (l.contains(nbts)) return; //no need to record a value twice
            comp.remove(key);
            comp.put(key, l);
        }
        else comp.putString(key, value);
        CommonIO.write(comp, dir, filename);
    }
}
