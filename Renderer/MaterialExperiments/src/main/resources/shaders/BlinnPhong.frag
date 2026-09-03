#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D tex;
uniform int useTexture;
uniform vec3 baseColor;

in vec3 worldNormal;
in vec3 worldVertex;
in vec2 texCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragWorldPos;

void main() {
    vec3 albedo = useTexture == 1
            ? texture(tex, texCoord).rgb
            : baseColor;

    fragColor = vec4(albedo, 1.0);
    fragNormal = vec4(normalize(worldNormal), 1.0);
    fragWorldPos = vec4(worldVertex, 1.0);
}
