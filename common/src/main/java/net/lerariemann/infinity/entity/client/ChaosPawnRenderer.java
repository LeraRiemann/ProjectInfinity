package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.client.state.ChaosPawnRenderState;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

public class ChaosPawnRenderer extends BipedEntityRenderer<ChaosPawn, ChaosPawnRenderState, BipedEntityModel<ChaosPawnRenderState>> {
    private static final Identifier TEXTURE = InfinityMethods.getId("textures/entity/empty.png");
    public ChaosPawnRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.25f);
        this.addFeature(new ChaosPawnTint(this, new BipedEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER))));
    }

    @Override
    public ChaosPawnRenderState createRenderState() {
        return new ChaosPawnRenderState();
    }

    @Override
    public Identifier getTexture(ChaosPawnRenderState state) {
        return TEXTURE;
    }
}
