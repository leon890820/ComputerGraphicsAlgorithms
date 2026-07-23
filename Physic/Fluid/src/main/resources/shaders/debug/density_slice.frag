#version 330

uniform sampler3D volumeTexture;
uniform float sliceDepth;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

vec3 heatmap(float value) {
    vec3 cold = vec3(0.02, 0.05, 0.12);
    vec3 blue = vec3(0.05, 0.28, 0.80);
    vec3 cyan = vec3(0.00, 0.86, 0.90);
    vec3 yellow = vec3(1.00, 0.82, 0.18);
    vec3 white = vec3(1.00, 0.98, 0.85);

    vec3 color = mix(cold, blue, smoothstep(0.00, 0.25, value));
    color = mix(color, cyan, smoothstep(0.20, 0.50, value));
    color = mix(color, yellow, smoothstep(0.45, 0.78, value));
    color = mix(color, white, smoothstep(0.72, 1.00, value));
    return color;
}

void main() {
    float density = texture(volumeTexture, vec3(texcoord, clamp(sliceDepth, 0.0, 1.0))).x;
    float normalizedDensity = 1.0 - exp(-density * 0.35);

    vec2 borderUv = min(texcoord, 1.0 - texcoord);
    float border = 1.0 - smoothstep(0.0, 0.018, min(borderUv.x, borderUv.y));

    vec3 color = heatmap(normalizedDensity);
    color = mix(color, vec3(0.9, 0.95, 1.0), border * 0.75);

    fragColor = vec4(color, 1.0);
}
