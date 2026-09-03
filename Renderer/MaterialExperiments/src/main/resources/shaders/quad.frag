#version 330

uniform sampler3D volumeTexture;
uniform float sliceDepth;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 volumeUv = vec3(texcoord, clamp(sliceDepth, 0.0, 1.0));
    vec4 slice = texture(volumeTexture, volumeUv);

    vec2 centered = texcoord * 2.0 - 1.0;
    float grid = max(
        smoothstep(0.018, 0.0, abs(fract(texcoord.x * 8.0) - 0.5)),
        smoothstep(0.018, 0.0, abs(fract(texcoord.y * 8.0) - 0.5))
    );

    vec3 color = slice.rgb + vec3(0.05, 0.07, 0.09) * grid * (1.0 - slice.a);
    color *= 1.0 - 0.22 * smoothstep(0.72, 1.35, length(centered));

    fragColor = vec4(color, 1.0);
}