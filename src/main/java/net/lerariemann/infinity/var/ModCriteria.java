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
    public static class DimensionOpenedCriterion extends AbstractCriterion<DimensionOpenedCriterion.Conditions> {
        static final Identifier ID = InfinityMod.getId("dims_open");

        public DimensionOpenedCriterion() {
            super();
        }

        public Identifier getId() {
            return ID;
        }

        @Override
        public DimensionOpenedCriterion.Conditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
            int score = jsonObject.getAsJsonPrimitive("amount").getAsInt();
            return new DimensionOpenedCriterion.Conditions(lootContextPredicate, score);
        }

        public void trigger(ServerPlayerEntity player) {
            this.trigger(player, (conditions) -> conditions.test(player.getStatHandler().getStat(ModStats.DIMS_OPENED_STAT)));
        }

        public static class Conditions extends AbstractCriterionConditions {
            private final int score;

            public Conditions(LootContextPredicate player, int score) {
                super(DimensionOpenedCriterion.ID, player);
                this.score = score;
            }

            public boolean test(int stat) {
                return stat > this.score;
            }

            public static DimensionOpenedCriterion.Conditions create(int i) {
                return new DimensionOpenedCriterion.Conditions(LootContextPredicate.EMPTY, i);
            }

            public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
                JsonObject jsonObject = super.toJson(predicateSerializer);
                jsonObject.add("amount", new JsonPrimitive(this.score));
                return jsonObject;
            }
        }
    }

    public static DimensionOpenedCriterion DIMS_OPENED;

    public static void registerCriteria() {
        DIMS_OPENED = Criteria.register(new DimensionOpenedCriterion());
    }
}
