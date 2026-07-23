#version 330

uniform sampler3D densityTexture;
uniform mat4 inverseProjection;
uniform mat4 cameraToWorld;
uniform vec3 cameraPosition;
uniform vec3 boundsCenter;
uniform vec3 boundsSize;
uniform float stepSize;
uniform float densityMultiplier;
uniform float densityOffset;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

const float TINY_NUDGE = 0.0001;
const int MAX_STEPS = 256;

vec2 rayBox(vec3 boundsMin, vec3 boundsMax, vec3 rayOrigin, vec3 invRayDir) {
    vec3 t0 = (boundsMin - rayOrigin) * invRayDir;
    vec3 t1 = (boundsMax - rayOrigin) * invRayDir;
    vec3 tMin = min(t0, t1);
    vec3 tMax = max(t0, t1);

    float dstToBox = max(max(tMin.x, tMin.y), tMin.z);
    float dstInsideBox = min(tMax.x, min(tMax.y, tMax.z)) - max(dstToBox, 0.0);
    return vec2(dstToBox, dstInsideBox);
}

vec3 getRayDirection(vec2 uv) {
    vec2 ndc = uv * 2.0 - 1.0;
    vec4 view = inverseProjection * vec4(ndc, -1.0, 1.0);
    view.xyz /= max(abs(view.w), 0.00001);
    return normalize((cameraToWorld * vec4(normalize(view.xyz), 0.0)).xyz);
}

void main() {
    vec3 boundsMin = boundsCenter - boundsSize * 0.5;
    vec3 boundsMax = boundsCenter + boundsSize * 0.5;
    vec3 rayDir = getRayDirection(texcoord);
    vec3 invRayDir = 1.0 / max(abs(rayDir), vec3(0.00001)) * sign(rayDir);
    vec2 hit = rayBox(boundsMin, boundsMax, cameraPosition, invRayDir);

    if (hit.y <= 0.0) {
        discard;
    }

    float dstToBox = max(hit.x, 0.0);
    float dstThroughBox = hit.y;
    vec3 entryPoint = cameraPosition + rayDir * (dstToBox + TINY_NUDGE);

    float densityAlongViewRay = 0.0;

    for (int i = 0; i < MAX_STEPS; i++) {
        float dst = float(i) * stepSize;

        if (dst >= dstThroughBox - TINY_NUDGE * 2.0) {
            break;
        }

        vec3 samplePos = entryPoint + rayDir * dst;
        vec3 volumeUv = (samplePos - boundsMin) / boundsSize;
        float density = max(0.0, texture(densityTexture, clamp(volumeUv, 0.0, 1.0)).x + densityOffset);
        densityAlongViewRay += density * densityMultiplier * stepSize;
    }

    float alpha = clamp(densityAlongViewRay, 0.0, 1.0);

    if (alpha <= 0.01) {
        discard;
    }

    fragColor = vec4(vec3(alpha), alpha);
}
