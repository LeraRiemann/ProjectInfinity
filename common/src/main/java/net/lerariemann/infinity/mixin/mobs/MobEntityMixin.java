package net.lerariemann.infinity.mixin.mobs;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.entity.custom.ChaosSlime;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(method = "getLootTableKey", at = @At(value = "RETURN"), cancellable = true)
    private void mixin(CallbackInfoReturnable<Optional<RegistryKey<LootTable>>> cir) {
        var entity = (MobEntity) (Object) this;
        if (entity instanceof ChaosPawn pawn) {
            Identifier i = switch (pawn.getCase()) {
                case 0 -> Identifier.of("infinity:entities/chaos_pawn_black");
                case 1 -> Identifier.of("infinity:entities/chaos_pawn_white");
                default -> {
                    boolean bl = RandomProvider.rule("pawnsCanDropIllegalItems");
                    if (bl) yield Identifier.of(""); //loot is defined in dropEquipment instead
                    else yield Identifier.of(InfinityMod.provider.randomName(pawn.getRandom(), ConfigType.LOOT_TABLES));
                }
            };
            cir.setReturnValue(Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE, i)));
        } else if (entity instanceof ChaosSlime slime) {
            cir.setReturnValue(slime.getCore().getBlock().getLootTableKey());
        }
    }
}
