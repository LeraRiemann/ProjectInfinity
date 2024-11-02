package net.lerariemann.infinity.entity.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;

public class DimensionalSkeletonRenderer extends SkeletonEntityRenderer {
    public DimensionalSkeletonRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.addFeature(new TintedLayerRenderer<>(this, new SkeletonEntityModel<>(context.getPart(EntityModelLayers.SKELETON))));
    }
}
