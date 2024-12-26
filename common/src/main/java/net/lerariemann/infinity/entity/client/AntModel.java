// Made with Blockbench 4.11.2
package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.AntEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class AntModel<T extends AntEntity> extends SinglePartEntityModel<T> {
	public static final EntityModelLayer MODEL_LAYER =
		new EntityModelLayer(InfinityMethods.getId("ant"), "main");

	private final ModelPart body;
    private final ModelPart head;
	private final ModelPart front_left_leg;
	private final ModelPart center_left_leg;
	private final ModelPart back_left_leg;
	private final ModelPart front_right_leg;
	private final ModelPart center_right_leg;
	private final ModelPart back_right_leg;

	public AntModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = body.getChild("head");
		this.front_left_leg = body.getChild("front_left_leg");
		this.center_left_leg = body.getChild("center_left_leg");
		this.back_left_leg = body.getChild("back_left_leg");
		this.front_right_leg = body.getChild("front_right_leg");
		this.center_right_leg = body.getChild("center_right_leg");
		this.back_right_leg = body.getChild("back_right_leg");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();
		ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(0, 11).cuboid(-2.0F, -5.75F, 3.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(-1.0F, -4.0F, -5.0F, 2.0F, 2.0F, 9.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		body.addChild("head", ModelPartBuilder.create().uv(16, 11).cuboid(-1.5F, -1.75F, -3.0F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F))
				.uv(0, 0)
				.cuboid(2.0F, -3.75F, -1.0F, -1.0F, 3.0F, 1.0F, new Dilation(0.0F))
				.uv(0, 0)
				.cuboid(-1.0F, -3.75F, -1.0F, -1.0F, 3.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, -3.0F, -4.0F));

		body.addChild("front_left_leg", ModelPartBuilder.create()
				.uv(16, 17)
				.cuboid(-0.5F, -0.5F, -1.5F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.of(0.5F, -2.5F, -1.5F, 0.0F, 0.2182F, 0.3054F));
		body.addChild("center_left_leg", ModelPartBuilder.create()
				.uv(0, 19)
				.cuboid(-0.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.of(0.5F, -2.5F, -0.5F, 0.0F, 0.0F, 0.3054F));
		body.addChild("back_left_leg", ModelPartBuilder.create()
				.uv(16, 19)
				.cuboid(-0.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.of(0.5F, -2.5F, 1.5F, 0.0F, -0.2182F, 0.3054F));
		body.addChild("front_right_leg", ModelPartBuilder.create()
				.uv(0, 21)
				.cuboid(-6.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.of(-0.5F, -2.5F, -2.5F, 0.0F, -0.2182F, -0.3054F));
		body.addChild("center_right_leg", ModelPartBuilder.create()
				.uv(16, 21)
				.cuboid(-6.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.of(-0.5F, -2.5F, -0.5F, 0.0F, 0.0F, -0.3054F));
		body.addChild("back_right_leg", ModelPartBuilder.create()
				.uv(22, 0)
				.cuboid(-6.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.of(-0.5F, -2.5F, 1.5F, 0.0F, 0.2182F, -0.3054F));

		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart getPart() {
		return body;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.getPart().traverse().forEach(ModelPart::resetTransform);

		this.head.yaw = MathHelper.clamp(headYaw, -30.0F, 30.0F) * 0.017453292F;
		this.head.pitch = MathHelper.clamp(headPitch, -25.0F, 45.0F) * 0.017453292F;

		float pi = 3.1415927F;
		float speed = Math.min(0.5f, 3.0f * limbDistance);
		float yawFront = MathHelper.cos(limbAngle * 0.6662F) * speed;
		float yawCenter = MathHelper.cos(limbAngle * 0.6662F + pi / 4) * speed;
		float yawBack = MathHelper.cos(limbAngle * 0.6662F + pi / 2) * speed;
		front_left_leg.yaw += yawFront;
		center_left_leg.yaw += yawCenter;
		back_left_leg.yaw += yawBack;
		front_right_leg.yaw -= yawFront;
		center_right_leg.yaw -= yawCenter;
		back_right_leg.yaw -= yawBack;
	}
}