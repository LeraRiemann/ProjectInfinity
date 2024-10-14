package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;

public class DimensionalCreeperRenderer extends CreeperEntityRenderer {
    private static final Identifier TEXTURE2 = InfinityMod.getId("textures/entity/creeper.png");

    @Override
    public Identifier getTexture(CreeperEntityRenderState creeperEntityRenderState) {
        return TEXTURE2;
    }

    public DimensionalCreeperRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.addFeature(new TintedLayerRenderer<>(this, new CreeperEntityModel<>(context.getPart(EntityModelLayers.CREEPER))));
    }
}
