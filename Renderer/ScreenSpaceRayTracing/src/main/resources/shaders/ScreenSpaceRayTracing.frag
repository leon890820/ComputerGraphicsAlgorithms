#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;

uniform sampler2D albedoTex;
uniform sampler2D normalTex;
uniform sampler2D depthTex;
uniform sampler2D floorNormalMap;
uniform int hasNormalMap;

uniform vec3 cameraPos;
uniform float cameraFar;
uniform float u_RayLength = 10000;
uniform float u_WindowWidth;
uniform float u_WindowHeight;
uniform float u_Fuzz;
uniform int u_FuzzySampleCount;

const int MAX_FUZZY_SAMPLE_COUNT = 4;
const float PI = 3.14159265359;

in vec3 worldVertex;
in vec3 worldNormal;
in vec3 worldTangent;
in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

struct Ray{
    vec3 Origin;
    vec3 Direction;
    vec3 SurfaceNormal;
};

struct Result{
    bool IsHit;
    vec2 UV;
    vec3 Position;

    int IterationCount;
    bool outTest;
};

vec4 projectToScreenSpace(vec3 vPoint){
    return u_ProjectionMatrix * vec4(vPoint,1);
}

vec3 projectToViewSpace(vec3 vPointInWorldSpace){
    return vec3(u_ViewMatrix * vec4(vPointInWorldSpace,1));
}

vec3 worldSpaceToScreenSpace(vec3 worldPos){
    vec3 V = projectToViewSpace(worldPos);
    vec4 NDC = projectToScreenSpace(V);
    vec3 screenPos =  NDC.xyz / NDC.w;
    screenPos = screenPos * 0.5 + 0.5;
    return screenPos;
}

float distanceSquared(vec2 A, vec2 B){
    A -= B;
    return dot(A, A);
}

vec3 getSurfaceNormal(){
    vec3 N = normalize(worldNormal);
    if (hasNormalMap == 1) {
        vec3 T = normalize(worldTangent - N * dot(N, worldTangent));
        vec3 B = normalize(cross(N, T));
        mat3 TBN = mat3(T, B, N);
        vec3 tangentNormal = texture(floorNormalMap, texCoord).rgb * 2.0 - 1.0;
        N = normalize(TBN * tangentNormal);
    }
    return N;
}

float hash13(vec3 p){
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

vec3 randomInUnitSphere(int sampleIndex){
    vec3 seed = vec3(gl_FragCoord.xy, float(sampleIndex) + texCoord.x * 17.0 + texCoord.y * 31.0);
    float z = hash13(seed + 11.0) * 2.0 - 1.0;
    float a = hash13(seed + 23.0) * 2.0 * PI;
    float r = pow(hash13(seed + 37.0), 0.3333333);
    float xy = sqrt(max(0.0, 1.0 - z * z));
    return vec3(cos(a) * xy, sin(a) * xy, z) * r;
}

vec3 getFuzzyReflectionDirection(vec3 reflectionDir, vec3 normal, int sampleIndex){
    if (u_Fuzz <= 0.0001 || u_FuzzySampleCount <= 1) {
        return reflectionDir;
    }

    vec3 dir = normalize(reflectionDir + randomInUnitSphere(sampleIndex) * u_Fuzz);
    if (dot(dir, normal) <= 0.0) {
        dir = reflect(dir, normal);
    }
    return normalize(dir);
}

bool Query(vec2 rayZRange, vec2 uv, vec3 surfaceNormal){
    vec2 size = vec2(u_WindowWidth, u_WindowHeight);
    vec2 texelUV = (floor(uv) + vec2(0.5)) / size;
    float sceneViewZ = texture(depthTex, texelUV).r;

    // GBuffer background is cleared to 0; visible geometry in view space has negative z.
    if (sceneViewZ >= -0.0001)
        return false;

    vec3 sceneNormal = normalize(texture(normalTex, texelUV).rgb);
    float rayNearZ = rayZRange.x;
    float rayFarZ = rayZRange.y;
    float thickness = max(2.0, abs(rayNearZ) * 0.003);

    bool sameSurface = dot(sceneNormal, surfaceNormal) > 0.98 && abs(sceneViewZ - rayNearZ) < thickness * 2.0;
    if (sameSurface)
        return false;

    return sceneViewZ < rayNearZ + thickness && sceneViewZ > rayFarZ - thickness;
}

Result RayMarching(Ray vRay){
    Result result;
    result.IsHit = false;
    result.UV = vec2(0.0);
    result.Position = vec3(0.0);
    result.IterationCount = 0;
    result.outTest = false;

    vec3 Begin = vRay.Origin + vRay.Direction * 0.05;
    float rayLength = min(u_RayLength, cameraFar);
    vec3 End = vRay.Origin + vRay.Direction * rayLength;

    vec3 V0 = projectToViewSpace(Begin);
    vec3 V1 = projectToViewSpace(End);

    // Keep the projected end point in front of the camera. Otherwise H.w can flip sign
    // and the screen-space line can tear, which shows up as a horizontal no-hit band.
    float nearPlaneZ = -0.1;
    if (V1.z > nearPlaneZ) {
        float t = (nearPlaneZ - V0.z) / (V1.z - V0.z);
        V1 = mix(V0, V1, clamp(t, 0.0, 1.0));
    }

    vec4 H0 = projectToScreenSpace(V0);
    vec4 H1 = projectToScreenSpace(V1);

    float k0 = 1.0 / H0.w;
    float k1 = 1.0 / H1.w;

    vec3 Q0 = V0 * k0;
    vec3 Q1 = V1 * k1;

    vec2 P0 = H0.xy * k0;
    vec2 P1 = H1.xy * k1;
    vec2 Size = vec2(u_WindowWidth,u_WindowHeight);
    P0 = (P0 + 1) / 2 * Size;
    P1 = (P1 + 1) / 2 * Size;

    P1 += vec2((distanceSquared(P0, P1) < 0.0001) ? 0.01 : 0.0);
    vec2 Delta = P1 - P0;

    bool Permute = false;
    if (abs(Delta.x) < abs(Delta.y))
    {
        Permute = true;
        Delta = Delta.yx; P0 = P0.yx; P1 = P1.yx;
    }

    float StepDir = sign(Delta.x);
    float Invdx = StepDir / Delta.x;
    vec3  dQ = (Q1 - Q0) * Invdx;
    float dk = (k1 - k0) * Invdx;
    vec2  dP = vec2(StepDir, Delta.y * Invdx);
    float Stride = 1.0f;

    dP *= Stride; dQ *= Stride; dk *= Stride;

    P0 += dP; Q0 += dQ; k0 += dk;

    int Step = 0;
    int MaxStep = 5000;
    float k = k0;
    float EndX = P1.x * StepDir;
    vec3 Q = Q0;
    float prevZMaxEstimate = V0.z;

    for(vec2 P = P0; Step < MaxStep && P.x * StepDir <= EndX; Step++, P += dP, Q.z += dQ.z, k += dk)
    {
        result.UV = Permute ? P.yx : P;
        vec2 Depths;
        Depths.x = prevZMaxEstimate;
        Depths.y = (dQ.z * 0.5 + Q.z) / (dk * 0.5 + k);
        prevZMaxEstimate = Depths.y;
        if(Depths.x < Depths.y)
            Depths.xy = Depths.yx;
        if(result.UV.x > u_WindowWidth || result.UV.x < 0 || result.UV.y > u_WindowHeight || result.UV.y < 0){
            break;
        }
        result.IsHit = Query(Depths, result.UV, vRay.SurfaceNormal);
        if (result.IsHit){
            break;
        }
    }

    return result;
}

void main() {
    vec3 screenPos = worldSpaceToScreenSpace(worldVertex);
    vec3 albedo = texture(albedoTex, screenPos.xy).rgb;
    vec3 normal = getSurfaceNormal();

    vec3 camPosToWorldPos = normalize(cameraPos - worldVertex);
    vec3 reflectionDir = normalize(reflect(-camPosToWorldPos, normal));
    int sampleCount = clamp(u_FuzzySampleCount, 1, MAX_FUZZY_SAMPLE_COUNT);
    vec3 reflectedColor = vec3(0.0);

    for (int i = 0; i < MAX_FUZZY_SAMPLE_COUNT; i++) {
        if (i >= sampleCount) {
            break;
        }

        Ray ray;
        ray.Origin = worldVertex;
        ray.Direction = getFuzzyReflectionDirection(reflectionDir, normal, i);
        ray.SurfaceNormal = normal;

        Result result = RayMarching(ray);
        if(result.IsHit){
            reflectedColor += texture(albedoTex,result.UV / vec2(u_WindowWidth,u_WindowHeight)).xyz;
        }
        else{
            reflectedColor += albedo;
        }
    }

    fragColor = vec4(reflectedColor / float(sampleCount), 1);
}
