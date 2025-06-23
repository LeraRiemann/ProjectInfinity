package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.client.state.BishopEntityRenderState;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class BishopModel extends BipedEntityModel<BishopEntityRenderState> {
    public static final EntityModelLayer MODEL_LAYER =
            new EntityModelLayer(InfinityMethods.getId("bishop"), "main");
    public BishopModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("body",
                ModelPartBuilder.create().uv(16, 16)
                        .cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, Dilation.NONE),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        root.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, Dilation.NONE)
                        .uv(40, 0)
                        .cuboid(-3.0F, -10.0F, -3.0F, 6.0F, 3.0F, 6.0F, Dilation.NONE)
                        .uv(0, 57)
                        .cuboid(-2.0F, -13.0F, -2.0F, 4.0F, 3.0F, 4.0F, Dilation.NONE)
                        .uv(0, 0)
                        .cuboid(-1.0F, -17.0F, -1.0F, 2.0F, 4.0F, 2.0F, Dilation.NONE)
                        .uv(54, 59)
                        .cuboid(-1.0F, -19.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.5F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        root.addChild("hat", ModelPartBuilder.create()
                        .uv(32, 35)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        root.addChild("right_arm", ModelPartBuilder.create()
                        .uv(0, 16)
                        .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, Dilation.NONE),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
        root.addChild("left_arm", ModelPartBuilder.create()
                        .uv(16, 48)
                        .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, Dilation.NONE),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F));
        root.addChild("right_leg", ModelPartBuilder.create()
                        .uv(0, 16)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, Dilation.NONE),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F));
        root.addChild("left_leg", ModelPartBuilder.create()
                        .uv(16, 48)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, Dilation.NONE),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }
}
