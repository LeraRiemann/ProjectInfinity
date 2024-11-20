package net.lerariemann.infinity.var;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ModCriteria {
    public static class DimensionOpenedCriterion extends AbstractCriterion<ScoredConditions> {
        static final Identifier ID = InfinityMod.getId("dims_open");

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
        static final Identifier ID = InfinityMod.getId("dims_closed");

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


    public static class WhoRemainsCriterion extends AbstractCriterion<EmptyConditions> {
        static final Identifier ID = InfinityMod.getId("who_remains");

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

    public static class IridescentCriterion extends AbstractCriterion<EmptyConditions> {
        public void trigger(ServerPlayerEntity player) {
            this.trigger(player, (conditions) -> true);
        }

        @Override
        public Codec<EmptyConditions> getConditionsCodec() {
            return EmptyConditions.CODEC;
        }
    }

    public record EmptyConditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<EmptyConditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(EmptyConditions::player)
                        )
                        .apply(instance, EmptyConditions::new)
        );
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

    public static RegistrySupplier<DimensionOpenedCriterion> DIMS_OPENED;
    public static RegistrySupplier<DimensionClosedCriterion> DIMS_CLOSED;
    public static RegistrySupplier<WhoRemainsCriterion> WHO_REMAINS;
    public static RegistrySupplier<IridescentCriterion> IRIDESCENT;

    public static final DeferredRegister<Criterion<?>> CRITERIA = DeferredRegister.create(MOD_ID, RegistryKeys.CRITERION);

    public static void registerCriteria() {
        DIMS_OPENED = CRITERIA.register("dims_open", DimensionOpenedCriterion::new);
        DIMS_CLOSED = CRITERIA.register("dims_closed", DimensionClosedCriterion::new);
        WHO_REMAINS = CRITERIA.register("who_remains", WhoRemainsCriterion::new);
        IRIDESCENT = CRITERIA.register("iridescence", IridescentCriterion::new);
        CRITERIA.register();
    }
}
