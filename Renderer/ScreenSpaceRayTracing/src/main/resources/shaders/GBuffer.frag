#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform vec3 ambient_light;
uniform vec3 cameraPos;
uniform float cameraFar;
uniform sampler2D tex;

uniform mat4 u_ViewMatrix;

in vec3 worldNormal;
in vec3 worldVertex;
in vec2 texCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragWorldPos;
layout(location = 3) out vec4 worldDepth;

void main()
{
    float gamma = 2.2;

    // 讀取 Diffuse
    vec4 texColor = texture(tex, texCoord);

    // Alpha Cutout
    if (texColor.a != 1.0)
    discard;

    // sRGB -> Linear
    vec3 diffuseColor = pow(texColor.rgb, vec3(gamma));

    // Exponential Tone Mapping
    vec3 mapped = vec3(1.0) - exp(-diffuseColor);

    // Linear -> sRGB
    mapped = pow(mapped, vec3(1.0 / gamma));

    // Ambient
    fragColor = vec4(mapped * ambient_light, 1.0);

    // World Normal
    fragNormal = vec4(normalize(worldNormal), 1.0);

    // World Position
    fragWorldPos = vec4(worldVertex, 1.0);

    // Distance to Camera
    vec3 viewPos = (u_ViewMatrix * vec4(worldVertex, 1.0)).xyz;
    worldDepth = vec4(vec3(viewPos.z), 1.0);
}