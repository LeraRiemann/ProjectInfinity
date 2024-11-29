package net.lerariemann.infinity.block.entity.render;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;

import static net.minecraft.client.render.RenderPhase.ENABLE_LIGHTMAP;
import static net.minecraft.client.render.RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE;

public class ModRenderLayer {
    public static ShaderProgram iridescenceSolid;

    public static RenderLayer getIridescenceSolid() {
        return RenderLayer.of(
                "iridescence_solid",
                VertexFormats.POSITION,
                VertexFormat.DrawMode.QUADS,
                4194304,
                true,
                false,
                RenderLayer.MultiPhaseParameters.builder()
                        .lightmap(ENABLE_LIGHTMAP)
                        .program(new RenderPhase.ShaderProgram(ModRenderLayer::getIridSolid))
                        .texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true)
        );
    }

    public static ShaderProgram getIridSolid() {
        return iridescenceSolid;
    }

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(InfinityMod.getId("iridescence_solid"), VertexFormats.POSITION,
                    program -> iridescenceSolid = program);
        });
    }
}
