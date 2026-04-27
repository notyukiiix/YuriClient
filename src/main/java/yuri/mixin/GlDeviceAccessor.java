package yuri.mixin;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlDevice.class)
public interface GlDeviceAccessor {
    @Accessor("pipelineCache")
    Map<RenderPipeline, GlRenderPipeline> getPipelineCache();

    /** Key type is private in {@link GlDevice}; callers in {@code com.mojang.blaze3d.opengl} build keys. */
    @Accessor("shaderCache")
    Map<?, ?> getShaderCache();
}
