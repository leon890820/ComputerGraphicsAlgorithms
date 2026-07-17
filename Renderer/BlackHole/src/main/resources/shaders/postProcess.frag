#version 330

uniform sampler2D sceneTexture;
uniform samplerCube skybox;
uniform mat4 inverseProjection;
uniform mat4 inverseView;
uniform vec3 cameraPosition;
uniform vec3 blackholePosition;
uniform float schwarzschildRadius;
uniform float stepSize;
uniform int maxSteps;
uniform float innerRadius;
uniform float outerRadius;
uniform float thickness;
uniform float density;
uniform float diskFalloffPower;
uniform float diskMinBrightness;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

float mapRange(float n, float x1, float x2, float y1, float y2) {
    return (n - x1) * (y2 - y1) / (x2 - x1) + y1;
}

vec3 buildWorldRay(vec2 uv) {
    vec2 ndc = uv * 2.0 - 1.0;
    vec4 viewDir = inverseProjection * vec4(ndc, 1.0, 1.0);
    viewDir = vec4(viewDir.xy, -1.0, 0.0);
    return normalize((inverseView * viewDir).xyz);
}

vec3 sampleSkybox(vec3 rayDir) {
    rayDir.x = -rayDir.x;
    return texture(skybox, rayDir).rgb;
}

vec3 getAdiskColor(vec3 worldPos) {
    vec3 r = worldPos - blackholePosition;
    float rad = r.x * r.x + r.z * r.z;

    if (abs(r.y) < thickness && rad < outerRadius * outerRadius && rad > innerRadius * innerRadius) {
        float d = clamp(mapRange(sqrt(rad), innerRadius, outerRadius, 1.0, 0.0), 0.0, 1.0);
        float brightness = mix(diskMinBrightness, 1.0, pow(d, diskFalloffPower));
        return vec3(1.0) * density * brightness;
    }

    return vec3(0.0);
}

void main() {
    vec3 rayPos = cameraPosition;
    vec3 rayDir = buildWorldRay(texcoord);
    vec3 adiskColor = vec3(0.0);

    vec3 h = cross(rayPos - blackholePosition, rayDir);
    float h2 = dot(h, h);

    int steps = clamp(maxSteps, 1, 4096);
    for (int step = 0; step < 4096; step++) {
        if (step >= steps) {
            break;
        }

        rayPos += rayDir * stepSize;

        vec3 rVec = rayPos - blackholePosition;
        float r = length(rVec);

        if (r < schwarzschildRadius) {
            fragColor = vec4(adiskColor, 1.0);
            return;
        }

        float invR = 1.0 / max(r, 1e-4);
        vec3 rHat = rVec * invR;
        float invR2 = invR * invR;
        float gravMag = 1.5 * h2 * invR2 * invR2;
        vec3 acc = -rHat * gravMag;

        rayDir = normalize(rayDir + acc * stepSize);
        adiskColor += getAdiskColor(rayPos) * stepSize;
    }

    vec3 skyColor = sampleSkybox(rayDir);
    fragColor = vec4(skyColor + adiskColor, 1.0);
}