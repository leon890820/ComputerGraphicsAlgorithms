#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform vec3 ambient_light;
uniform vec3 cameraPos;
uniform float cameraFar;
uniform sampler2D tex;

in vec3 worldNormal;
in vec3 worldVertex;
in vec2 texCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragWorldPos;
layout(location = 3) out vec4 worldDepth;

void main() {  
    vec3 texture_color = texture(tex, texCoord).rgb;
    vec3 ambient = texture_color * ambient_light;
    vec3 color = ambient;

    fragColor = vec4(color, 1.0);
    fragNormal = vec4(worldNormal, 1.0);
    fragWorldPos = vec4(worldVertex, 1.0);

    float lightDistance = length(worldVertex - cameraPos);
    lightDistance = lightDistance / cameraFar;
    worldDepth = vec4(vec3(lightDistance), 1.0);
}