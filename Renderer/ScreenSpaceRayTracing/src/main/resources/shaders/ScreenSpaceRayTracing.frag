#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;

uniform sampler2D albedoTex;
uniform sampler2D normalTex;
uniform sampler2D depthTex;

uniform vec3 cameraPos;
uniform float cameraFar;
uniform float u_RayLength = 10000;
uniform float u_WindowWidth;
uniform float u_WindowHeight;

in vec3 worldVertex;

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

vec3 projectToViewSpace(vec3 vPointInViewSpace){
    return vec3(u_ViewMatrix * vec4(vPointInViewSpace,1));
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
    float thickness = max(0.01, abs(rayNearZ) * 0.001);

    bool sameSurface = dot(sceneNormal, surfaceNormal) > 0.98 && abs(sceneViewZ - rayNearZ) < thickness * 2.0;
    if (sameSurface)
        return false;

    return sceneViewZ < rayNearZ + thickness && sceneViewZ > rayFarZ - thickness;
}


bool IsOutSideScreen(vec3 pos){
    if(pos.x < 0 || pos.x > 1 || pos.y < 0 ||pos.y > 1 || pos.z < 0 || pos.z > 1) return true;
    return false;
}

Result RayMarching(Ray vRay){
    Result result;
    result.IsHit = false;
    result.UV = vec2(0.0);
    result.Position = vec3(0.0);
    result.IterationCount = 0;
    result.outTest = false;

    vec3 Begin = vRay.Origin;
    float rayLength = min(u_RayLength, cameraFar);
    vec3 End = vRay.Origin + vRay.Direction * rayLength;

    vec3 V0 = projectToViewSpace(Begin);
    vec3 V1 = projectToViewSpace(End);

    vec4 H0 = projectToScreenSpace(V0);
    vec4 H1 = projectToScreenSpace(V1);

    float k0 = 1.0 / H0.w;
    float k1 = 1.0 / H1.w;

    vec3 Q0 = V0 * k0;
    vec3 Q1 = V1 * k1;

    // NDC-space not Screen Space
    vec2 P0 = H0.xy * k0;
    vec2 P1 = H1.xy * k1;
    vec2 Size = vec2(u_WindowWidth,u_WindowHeight);
    //Screen Space
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

    for(vec2 P = P0;  Step < MaxStep;Step++,P += dP, Q.z += dQ.z, k += dk)
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
    vec3 normal = normalize(texture(normalTex, screenPos.xy).rgb);

    vec3 camPosToWorldPos = normalize(cameraPos - worldVertex);
    vec3 dir = normalize(reflect(-camPosToWorldPos, normal));

    Ray ray;
    ray.Origin = worldVertex;
    ray.Direction = dir;
    ray.SurfaceNormal = normal;

    Result result = RayMarching(ray);

    if(result.IsHit){
        fragColor = vec4(texture(albedoTex,result.UV / vec2(u_WindowWidth,u_WindowHeight)).xyz, 1);
    }
    else{
        fragColor = vec4(texture(albedoTex,screenPos.xy).xyz, 1);
    }

}