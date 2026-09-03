#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D albedo;
uniform sampler2D worldPos;
uniform sampler2D worldNormal;
uniform sampler2D shadowMap;

uniform mat4 lightSpaceMatrix;
uniform float lightFar;
uniform vec3 light_dir;
uniform vec3 light_pos;
uniform vec3 light_color;
uniform vec3 view_pos;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

float ShadowCalculation(vec3 worldVertex, vec3 N, vec3 L)
{
    vec4 fragPosLightSpace = lightSpaceMatrix * vec4(worldVertex, 1.0);
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    if (projCoords.x < 0.0 || projCoords.x > 1.0 ||
        projCoords.y < 0.0 || projCoords.y > 1.0 ||
        projCoords.z > 1.0)
    {
        return 0.0;
    }

    float currentDepth = length(worldVertex - light_pos);
    float closestDepth = texture(shadowMap, projCoords.xy).r * lightFar;
    float bias = max(0.015 * (1.0 - dot(N, L)), 0.015);
    return currentDepth - bias > closestDepth ? 0.7 : 0.0;
}

void main() {
    vec3 baseColor = texture(albedo, texcoord).rgb;
    vec3 worldVertex = texture(worldPos, texcoord).rgb;
    vec3 N = normalize(texture(worldNormal, texcoord).rgb);
    vec3 L = normalize(-light_dir);
    vec3 V = normalize(view_pos - worldVertex);
    vec3 H = normalize(L + V);

    float shadow = ShadowCalculation(worldVertex, N, L);

    vec3 ambient = baseColor * 0.12;
    vec3 diffuse = baseColor * light_color * max(0.0, dot(N, L));
    vec3 specular = light_color * 0.3 * pow(max(0.0, dot(N, H)), 64.0);
    vec3 color = ambient + (1.0 - shadow) * (diffuse + specular);

    fragColor = vec4(color, 1.0);
}
