package net.lerariemann.infinity.mixin.qol;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public class EditWorldScreenMixin {

    @Shadow @Final private BooleanConsumer callback;

    @Inject(method = "<init>", at = @At(value = "RETURN", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    void addButton(MinecraftClient client, LevelStorage.Session session, String levelName, BooleanConsumer callback, CallbackInfo ci) {
        var screen = (EditWorldScreen) (Object) this;
        screen.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.infinity.delete_datapacks"), button -> {
            boolean bl = InfinityMethods.deleteLevel(session);
            this.callback.accept(!bl);
        }).width(200).tooltip(Tooltip.of(Text.translatable("screen.infinity.delete_datapacks.tooltip"), null)).build());
    }
}
