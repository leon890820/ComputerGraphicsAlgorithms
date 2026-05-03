#version 440
#ifdef GL_ES
precision mediump float;
#endif

#define VPL_NUM 32

// =========================
// GBuffer
// =========================
uniform sampler2D u_AlbedoTexture;
uniform sampler2D u_NormalTexture;
uniform sampler2D u_PositionTexture;

// =========================
// RSM Buffer
// =========================
uniform sampler2D u_RSMFluxTexture;
uniform sampler2D u_RSMNormalTexture;
uniform sampler2D u_RSMPositionTexture;
uniform sampler2D u_RSMDepthTexture;

// =========================
// Light / RSM Parameters
// =========================
uniform mat4  u_LightVPMatrix;
uniform vec3  u_LightDirInWorldSpace;
uniform vec3  u_LightPosInWorldSpace;

uniform float u_MaxSampleRadius;
uniform int   u_RSMSize;
uniform int   u_VPLNum;
uniform float lightFar;

// 0 = 關閉 RSM indirect, 1 = 開啟
uniform int RTX;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

layout(std430, binding = 0) buffer VPLsSampleCoordsAndWeights {
    vec4 u_VPLsSampleCoordsAndWeights[VPL_NUM];
};

// =========================
// Utility
// =========================

bool isOutside01(vec3 v)
{
    return v.x < 0.0 || v.x > 1.0 ||
    v.y < 0.0 || v.y > 1.0 ||
    v.z < 0.0 || v.z > 1.0;
}

bool isOutside01(vec2 v){
    return v.x < 0.0 || v.x > 1.0 ||
    v.y < 0.0 || v.y > 1.0;
}

vec3 projectToLightTexCoord(vec3 worldPos){
    vec4 lightClip = u_LightVPMatrix * vec4(worldPos, 1.0);
    vec3 lightNDC  = lightClip.xyz / lightClip.w;

    return lightNDC * 0.5 + 0.5;
}

float calcShadow(vec3 worldPos, vec3 fragLightTexCoord){
    float sum = 0.0;
    float texelSize = 1.0 / float(u_RSMSize);
    for(int i = 0; i < 32; i++){
        vec2 lightTexCoord = fragLightTexCoord.xy + u_VPLsSampleCoordsAndWeights[i].xy * 10 * texelSize;
        if (isOutside01(lightTexCoord))
            return 1.0;

        float rsmDepth = texture(u_RSMDepthTexture, lightTexCoord).r;

        vec3 fragToLight = worldPos - u_LightPosInWorldSpace;
        float currentDepth = length(fragToLight) / lightFar;

        float bias = 0.01;
        sum += abs(currentDepth - rsmDepth) > bias ? 0.0 : 1.0;
    }

    return sum / 32.0;
}

vec3 calcVPLIrradiance(vec3 vplFlux, vec3 vplNormal, vec3 vplPos, vec3 fragPos, vec3 fragNormal, float weight){
    vec3 vplToFrag = normalize(fragPos - vplPos);

    float vplCos  = max(dot(vplNormal,  vplToFrag), 0.0);
    float fragCos = max(dot(fragNormal, -vplToFrag), 0.0);

    return vplFlux * vplCos * fragCos * weight;
}

vec3 calcIndirectIllumination(vec2 fragLightUV, vec3 fragPos, vec3 fragNormal, vec3 fragAlbedo){
    vec3 indirect = vec3(0.0);

    float texelSize = 1.0 / float(u_RSMSize);

    for (int i = 0; i < u_VPLNum; i++){
        vec4 sampleData = u_VPLsSampleCoordsAndWeights[i];

        vec2 offset = sampleData.xy * u_MaxSampleRadius * texelSize;
        float weight = sampleData.z;

        vec2 vplUV = fragLightUV + offset;

        if (isOutside01(vplUV))
        continue;

        vec3 vplFlux   = texture(u_RSMFluxTexture, vplUV).xyz;
        vec3 vplNormal = normalize(texture(u_RSMNormalTexture, vplUV).xyz);
        vec3 vplPos    = texture(u_RSMPositionTexture, vplUV).xyz;

        indirect += calcVPLIrradiance(vplFlux, vplNormal, vplPos, fragPos, fragNormal, weight);
    }

    indirect *= fragAlbedo;
    indirect /= float(u_VPLNum);
    indirect *= 20.0;

    return indirect;
}

// =========================
// Main
// =========================

void main()
{
    vec3 fragNormal = normalize(texture(u_NormalTexture, texCoord).xyz);
    vec3 fragAlbedo = texture(u_AlbedoTexture, texCoord).xyz;
    vec3 fragPos    = texture(u_PositionTexture, texCoord).xyz;

    vec3 fragLightTexCoord = projectToLightTexCoord(fragPos);
    vec2 fragLightUV       = fragLightTexCoord.xy;

    bool outsideLightFrustum = isOutside01(fragLightTexCoord);

    // =========================
    // Direct Illumination
    // =========================
    vec3 ambient = fragAlbedo * 0.1;

    float NdotL = max(dot(fragNormal, -u_LightDirInWorldSpace), 0.0);
    vec3 diffuse = fragAlbedo * NdotL;

    float shadow = calcShadow(fragPos, fragLightTexCoord);

    vec3 directIllumination = ambient;

    if (!outsideLightFrustum)
    directIllumination += diffuse * shadow;

    // =========================
    // Indirect Illumination - RSM
    // =========================
    vec3 indirectIllumination = vec3(0.0);

    if (RTX > 0){
        indirectIllumination = calcIndirectIllumination(fragLightUV, fragPos,fragNormal, fragAlbedo);
    }

    vec3 result = directIllumination + indirectIllumination;

    fragColor = vec4(result, 1.0);
}