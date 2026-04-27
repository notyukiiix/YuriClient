package yuri.client;

import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Constructor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.ResourceLocation;
import yuri.mixin.GlDeviceAccessor;

/**
 * Drops compiled lightmap shaders from {@link com.mojang.blaze3d.opengl.GlDevice} caches so they are rebuilt.
 * Uses reflection for {@code GlDevice.ShaderCompilationKey} because that record is private to {@code GlDevice}.
 */
public final class FullbrightLightmapCacheUtil {
    private static final String SHADER_KEY_CLASS = "com.mojang.blaze3d.opengl.GlDevice$ShaderCompilationKey";

    private FullbrightLightmapCacheUtil() {
    }

    public static void invalidateLightmapCaches() {
        var gpu = RenderSystem.tryGetDevice();
        if (!(gpu instanceof GlDeviceAccessor acc)) {
            return;
        }
        var lightmap = RenderPipelines.LIGHTMAP;
        acc.getPipelineCache().remove(lightmap);
        Object key = newShaderCompilationKey(
            lightmap.getFragmentShader(),
            ShaderType.FRAGMENT,
            lightmap.getShaderDefines()
        );
        if (key != null) {
            acc.getShaderCache().remove(key);
        }
    }

    private static Object newShaderCompilationKey(
        ResourceLocation id,
        ShaderType type,
        ShaderDefines defines
    ) {
        try {
            Class<?> keyClass = Class.forName(SHADER_KEY_CLASS);
            Constructor<?> ctor = keyClass.getDeclaredConstructor(ResourceLocation.class, ShaderType.class, ShaderDefines.class);
            ctor.setAccessible(true);
            return ctor.newInstance(id, type, defines);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
