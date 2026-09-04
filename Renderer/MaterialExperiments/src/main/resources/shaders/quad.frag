#version 330

uniform sampler3D volumeTexture;
uniform float sliceDepth;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 volumeUv = vec3(texcoord, clamp(sliceDepth, 0.0, 1.0));
    vec4 slice = texture(volumeTexture, volumeUv);

    vec2 centered = texcoord * 2.0 - 1.0;
    vec3 color = slice.rgb;
    color *= 1.0 - 0.18 * smoothstep(0.72, 1.35, length(centered));

    fragColor = vec4(color, 1.0);
}
