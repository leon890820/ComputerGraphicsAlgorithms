#version 330

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D texNoise;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 samples[64];
uniform int kernelSize;
uniform float radius;
uniform float bias;
uniform float power;
uniform vec2 noiseScale;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

float linearDepthFromWorld(vec3 worldPos) {
    vec3 viewPos = (viewMatrix * vec4(worldPos, 1.0)).xyz;
    return -viewPos.z;
}

vec2 projectWorldToUv(vec3 worldPos) {
    vec4 clip = projectionMatrix * viewMatrix * vec4(worldPos, 1.0);
    if (clip.w <= 0.0) {
        return vec2(-1.0);
    }
    clip.xyz /= clip.w;
    return clip.xy * 0.5 + 0.5;
}

float computeSSAO(vec3 origin, vec3 normal) {
    vec3 randomVec = texture(texNoise, texcoord * noiseScale).xyz * 2.0 - 1.0;
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 tbn = mat3(tangent, bitangent, normal);

    float originDepth = linearDepthFromWorld(origin);
    float occlusion = 0.0;
    float validSamples = 0.0;

    for (int i = 0; i < kernelSize; i++) {
        vec3 samplePos = origin + (tbn * samples[i]) * radius;
        float sampleDepth = linearDepthFromWorld(samplePos);

        if (sampleDepth <= 0.0) {
            continue;
        }

        vec2 sampleUv = projectWorldToUv(samplePos);

        if (sampleUv.x < 0.0 || sampleUv.x > 1.0 || sampleUv.y < 0.0 || sampleUv.y > 1.0) {
            continue;
        }

        vec4 sampledPositionDepth = texture(gPosition, sampleUv);
        if (sampledPositionDepth.a <= 0.0) {
            continue;
        }

        float sampledLinearDepth = sampledPositionDepth.a;
        float depthDelta = abs(originDepth - sampledLinearDepth);
        float rangeCheck = smoothstep(0.0, 1.0, radius / max(depthDelta, 0.0001));

        occlusion += (sampledLinearDepth <= sampleDepth - bias ? 1.0 : 0.0) * rangeCheck;
        validSamples += 1.0;
    }

    if (validSamples <= 0.0) {
        return 1.0;
    }

    float ao = 1.0 - occlusion / validSamples;
    return pow(clamp(ao, 0.0, 1.0), power);
}

void main() {
    vec4 positionDepth = texture(gPosition, texcoord);
    if (positionDepth.a <= 0.0) {
        fragColor = vec4(1.0);
        return;
    }

    vec3 worldPos = positionDepth.xyz;
    vec3 worldNormal = normalize(texture(gNormal, texcoord).xyz);
    float ao = computeSSAO(worldPos, worldNormal);

    fragColor = vec4(vec3(ao), 1.0);
}
