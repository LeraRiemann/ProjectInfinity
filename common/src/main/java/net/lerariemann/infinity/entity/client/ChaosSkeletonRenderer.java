package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.ChaosSkeleton;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;

public class ChaosSkeletonRenderer extends SkeletonEntityRenderer {
    public ChaosSkeletonRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.addFeature(new TintedLayerRenderer<>(this, new SkeletonEntityModel<>(context.getPart(EntityModelLayers.SKELETON))));
    }
}
