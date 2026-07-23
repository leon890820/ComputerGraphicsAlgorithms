#version 330

uniform sampler2D faceTexture;
uniform vec3 view_pos;
uniform vec4 eyeColor;
uniform vec4 rimColor;
uniform float maskThreshold;

in vec3 worldNormal;
in vec3 worldVertex;
in vec2 texCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragWorldPos;

void main() {
    vec4 tex = texture(faceTexture, texCoord);
    float mask = max(max(tex.r, tex.g), tex.b);

    if (mask <= maskThreshold) {
        discard;
    }

    vec3 normal = normalize(worldNormal);
    vec3 viewDir = normalize(view_pos - worldVertex);
    float facing = clamp(dot(normal, viewDir), 0.0, 1.0);
    float rim = smoothstep(0.0, 0.55, 1.0 - facing);
    vec3 color = mix(eyeColor.rgb, rimColor.rgb, rim * rimColor.a);

    fragColor = vec4(color, mask * eyeColor.a);
    fragNormal = vec4(normal, 1.0);
    fragWorldPos = vec4(worldVertex, 1.0);
}
