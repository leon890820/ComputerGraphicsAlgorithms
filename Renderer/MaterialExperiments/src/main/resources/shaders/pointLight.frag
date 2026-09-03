#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D albedo;
uniform sampler2D worldPos;
uniform sampler2D worldNormal;
uniform samplerCube shadowCubeMap;

uniform vec3 light_pos;
uniform vec3 light_color;
uniform vec3 view_pos;
uniform float lightFar;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

float ShadowCalculationPoint(vec3 fragPos, vec3 N, vec3 L)
{
    vec3 fragToLight = fragPos - light_pos;
    float currentDepth = length(fragToLight);

    float closestDepth = texture(shadowCubeMap, fragToLight).r * lightFar;
    if (closestDepth <= 0.0001) {
        return 0.0;
    }

    float bias = max(0.03 * (1.0 - dot(N, L)), 0.01);
    return currentDepth - bias > closestDepth ? 1.0 : 0.0;
}

void main() {
    vec3 baseColor = texture(albedo, texcoord).rgb;
    vec3 worldVertex = texture(worldPos, texcoord).rgb;
    vec3 N = normalize(texture(worldNormal, texcoord).rgb);

    vec3 lightVec = light_pos - worldVertex;
    float dist = length(lightVec);
    vec3 L = lightVec / max(dist, 0.0001);
    vec3 V = normalize(view_pos - worldVertex);
    vec3 H = normalize(L + V);

    float shadow = ShadowCalculationPoint(worldVertex, N, L);
    float attenuation = 1.0 / (1.0 + 0.03 * dist + 0.002 * dist * dist);

    vec3 ambient = baseColor * 0.08;
    vec3 diffuse = baseColor * light_color * max(0.0, dot(N, L)) * attenuation;
    vec3 specular = light_color * 0.35 * pow(max(0.0, dot(N, H)), 64.0) * attenuation;
    vec3 color = ambient + (1.0 - shadow) * (diffuse + specular);

    fragColor = vec4(color, 1.0);
}
