package net.lerariemann.infinity.registry.var;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.BiomeBottleBlockEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ModCriteria {
    public static class DimensionOpenedCriterion extends AbstractCriterion<ScoredConditions> {
        static final Identifier ID = InfinityMethods.getId("dims_open");

        public DimensionOpenedCriterion() {
            super();
        }

        public Identifier getId() {
            return ID;
        }

        @Override
        public ScoredConditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
            int score = jsonObject.getAsJsonPrimitive("amount").getAsInt();
            return new ScoredConditions(lootContextPredicate, score, getId());
        }

        public void trigger(ServerPlayerEntity player) {
            this.trigger(player, (conditions) -> conditions.test(player.getStatHandler().getStat(ModStats.DIMS_OPENED_STAT)));
        }
    }

    public static class DimensionClosedCriterion extends AbstractCriterion<ScoredConditions> {
        static final Identifier ID = InfinityMethods.getId("dims_closed");

        public DimensionClosedCriterion() {
            super();
        }

        public Identifier getId() {
            return ID;
        }

        @Override
        public ScoredConditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
            int score = jsonObject.getAsJsonPrimitive("amount").getAsInt();
            return new ScoredConditions(lootContextPredicate, score, getId());
        }

        public void trigger(ServerPlayerEntity player) {
            this.trigger(player, (conditions) -> conditions.test(player.getStatHandler().getStat(ModStats.WORLDS_DESTROYED_STAT)));
        }
    }

    public static class BiomeBottleCriterion extends AbstractCriterion<ScoredConditions> {
        static final Identifier ID = InfinityMethods.getId("bottle");

        public BiomeBottleCriterion() {
            super();
        }

        public Identifier getId() {
            return ID;
        }

        @Override
        public ScoredConditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
            int score = jsonObject.getAsJsonPrimitive("amount").getAsInt();
            return new ScoredConditions(lootContextPredicate, score, getId());
        }

        public void trigger(ServerPlayerEntity player, BiomeBottleBlockEntity be) {
            this.trigger(player, (conditions) -> conditions.test(be.charge));
        }
    }

    public static class WhoRemainsCriterion extends AbstractCriterion<EmptyConditions> {
        static final Identifier ID = InfinityMethods.getId("who_remains");

        public WhoRemainsCriterion() {
            super();
        }

        public Identifier getId() {
            return ID;
        }

        @Override
        public EmptyConditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
            return new EmptyConditions(lootContextPredicate, ID);
        }

        public void trigger(ServerPlayerEntity player) {
            this.trigger(player, (conditions) -> true);
        }
    }

    public static class IridescentCriterion extends AbstractCriterion<IridescenceConditions> {
        public void trigger(ServerPlayerEntity player, boolean willing, int level) {
            this.trigger(player, (conditions) -> conditions.test(willing, level));
        }

        @Override
        public Codec<IridescenceConditions> getConditionsCodec() {
            return IridescenceConditions.CODEC;
        }
    }

    public static class ConvertMobCriterion extends AbstractCriterion<DataConditions> {
        static final Identifier ID = InfinityMethods.getId("convert_mob");


        public void trigger(ServerPlayerEntity player, LivingEntity e) {
            this.trigger(player, (conditions) -> conditions.test(Integer.parseInt(Registries.ENTITY_TYPE.getId(e.getType()).toString())));
        }

        @Override
        protected DataConditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
            return null;
        }

        @Override
        public Identifier getId() {
            return ID;
        }
    }

    public static class EmptyConditions extends AbstractCriterionConditions {

        public EmptyConditions(LootContextPredicate player, Identifier ID) {
            super(ID, player);
        }

        public static EmptyConditions create(Identifier ID) {
            return new EmptyConditions(LootContextPredicate.EMPTY, ID);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            return super.toJson(predicateSerializer);
        }
    }

    public static class ScoredConditions extends AbstractCriterionConditions {
        private final int score;

        public ScoredConditions(LootContextPredicate player, int score, Identifier ID) {
            super(ID, player);
            this.score = score;
        }

        public boolean test(int stat) {
            return stat >= this.score;
        }

        public static ScoredConditions create(int i, Identifier ID) {
            return new ScoredConditions(LootContextPredicate.EMPTY, i, ID);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("amount", new JsonPrimitive(this.score));
            return jsonObject;
        }
    }

    public record IridescenceConditions(Optional<LootContextPredicate> player, Optional<Boolean> willing, Optional<Integer> minLevel) implements AbstractCriterion.Conditions {
        public static final Codec<IridescenceConditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(IridescenceConditions::player),
                                Codec.BOOL.optionalFieldOf("willing").forGetter(IridescenceConditions::willing),
                                Codec.INT.optionalFieldOf("min_level").forGetter(IridescenceConditions::minLevel)
                        )
                        .apply(instance, IridescenceConditions::new)
        );

        public boolean test(boolean isWilling, int level) {
            return (willing.isEmpty() || isWilling == willing.get()) && (minLevel.isEmpty() || level >= minLevel.get());
        }
    }

    public static RegistrySupplier<DimensionOpenedCriterion> DIMS_OPENED;
    public static RegistrySupplier<DimensionClosedCriterion> DIMS_CLOSED;
    public static RegistrySupplier<WhoRemainsCriterion> WHO_REMAINS;
    public static RegistrySupplier<IridescentCriterion> IRIDESCENT;
    public static RegistrySupplier<BiomeBottleCriterion> BIOME_BOTTLE;
    public static RegistrySupplier<ConvertMobCriterion> CONVERT_MOB;

        public DataConditions(LootContextPredicate player, int score, Identifier ID) {
            super(ID, player);
            this.score = score;
        }

        public boolean test(int stat) {
            return stat == this.score;
        }

        public static ScoredConditions create(int i, Identifier ID) {
            return new ScoredConditions(LootContextPredicate.EMPTY, i, ID);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("amount", new JsonPrimitive(this.score));
            return jsonObject;
        }
    }

    public static DimensionOpenedCriterion DIMS_OPENED;
    public static DimensionClosedCriterion DIMS_CLOSED;
    public static WhoRemainsCriterion WHO_REMAINS;
    public static IridescentCriterion IRIDESCENT;
    public static BiomeBottleCriterion BIOME_BOTTLE;
    public static ConvertMobCriterion CONVERT_MOB;

    public static void registerCriteria() {
        DIMS_OPENED = Criteria.register(new DimensionOpenedCriterion());
        DIMS_CLOSED = Criteria.register(new DimensionClosedCriterion());
        WHO_REMAINS = Criteria.register(new WhoRemainsCriterion());
        IRIDESCENT = Criteria.register(new IridescentCriterion());
        BIOME_BOTTLE = Criteria.register(new BiomeBottleCriterion());
        CONVERT_MOB = Criteria.register(new ConvertMobCriterion());
    }
}