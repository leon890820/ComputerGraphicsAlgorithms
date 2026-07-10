#version 330

uniform sampler2D tex;
uniform int useTexture;

in vec2 texCoord;
in vec3 prtColor;

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 albedo = useTexture == 1 ? texture(tex, texCoord).rgb : vec3(1.0);
    fragColor = vec4(prtColor * albedo * 1.2f, 1.0);
}
