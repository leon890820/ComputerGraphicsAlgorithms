#version 330

uniform sampler2D albedoTex;
uniform sampler2D normalTex;
uniform sampler2D positionTex;
uniform sampler2D rawSSAOTex;
uniform sampler2D ssaoTex;
uniform sampler2D edgeTex;

uniform vec3 light_pos;
uniform vec3 light_dir;
uniform vec3 light_color;
uniform vec3 view_pos;
uniform int lightType;
uniform int useSSAO;
uniform float time;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

vec3 lightingDirection(vec3 worldPos) {
    if (lightType == 1) {
        return normalize(light_pos - worldPos);
    }

    if (lightType == 2) {
        return normalize(-light_dir);
    }

    return normalize(vec3(0.4, 0.7, 0.5));
}

vec3 modeColor(int mode, vec3 albedo, vec3 normal, vec3 ambient, vec3 diffuse, vec3 specular, vec3 edges, float rawAo, float ao) {
    if (mode == 0) {
        return ambient + diffuse + specular;
    }

    if (mode == 1) {
        return edges;
    }

    if (mode == 2) {
        return vec3(rawAo);
    }

    if (mode == 3) {
        return vec3(ao);
    }

    if (mode == 4) {
        return normal * 0.5 + 0.5;
    }

    if (mode == 5) {
        return albedo * 0.5;
    }

    if (mode == 6) {
        return ambient;
    }

    return ambient + diffuse;
}

void main() {
    vec3 albedo = texture(albedoTex, texcoord).rgb;
    vec3 worldPos = texture(positionTex, texcoord).rgb;
    vec3 normal = normalize(texture(normalTex, texcoord).rgb);
    float rawAo = useSSAO == 1 ? texture(rawSSAOTex, texcoord).r : 1.0;
    float ao = useSSAO == 1 ? texture(ssaoTex, texcoord).r : 1.0;
    vec3 edges = texture(edgeTex, texcoord).rgb;

    vec3 ambient = albedo * 0.5 * ao;

    vec3 L = lightingDirection(worldPos);
    vec3 V = normalize(view_pos - worldPos);
    vec3 H = normalize(L + V);
    vec3 diffuse = albedo * 0.7 * light_color * max(0.0, dot(normal, L));
    vec3 specular = 0.3 * light_color * pow(max(0.0, dot(normal, H)), 64.0);

    float stageDuration = 3.0208333;
    float sweepDuration = 0.55;
    float stageTime = time / stageDuration;
    int currentMode = int(mod(floor(stageTime), 8.0));
    int nextMode = int(mod(float(currentMode + 1), 8.0));
    float stageProgress = fract(stageTime);
    float holdEnd = 1.0 - sweepDuration / stageDuration;
    float sweep = smoothstep(holdEnd, 1.0, stageProgress);
    float isSweeping = step(holdEnd, stageProgress);

    vec3 currentColor = modeColor(currentMode, albedo, normal, ambient, diffuse, specular, edges, rawAo, ao);
    vec3 nextColor = modeColor(nextMode, albedo, normal, ambient, diffuse, specular, edges, rawAo, ao);

    float edgeWidth = 0.006;
    float nextMask = step(texcoord.x, sweep);
    vec3 color = mix(currentColor, nextColor, nextMask);
    float divider = 1.0 - smoothstep(0.0, edgeWidth, abs(texcoord.x - sweep));
    color = mix(color, vec3(1.0), divider * 0.85 * isSweeping);

    fragColor = vec4(color, 1.0);
}
