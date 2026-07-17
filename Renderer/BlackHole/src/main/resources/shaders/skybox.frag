#version 330

uniform samplerCube skybox;
uniform mat4 inverseProjection;
uniform mat4 inverseView;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 ndc = texcoord * 2.0 - 1.0;
    vec4 viewDir = inverseProjection * vec4(ndc, 1.0, 1.0);
    viewDir = vec4(viewDir.xy, -1.0, 0.0);

    vec3 worldDir = normalize((inverseView * viewDir).xyz);
    worldDir.x = -worldDir.x;
    fragColor = texture(skybox, worldDir);
}
