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
uniform samplerCube u_RSMFluxTexture;
uniform samplerCube u_RSMNormalTexture;
uniform samplerCube u_RSMPositionTexture;
uniform samplerCube u_RSMDepthTexture;

// =========================
// Light / RSM Parameters
// =========================
uniform mat4  u_LightVPMatrix;
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


float calcShadow(vec3 worldPos){
    float sum = 0.0;

    for (int i = 0; i < u_VPLNum; i++){
        vec3 sampleData = u_VPLsSampleCoordsAndWeights[i].xyz;
        vec3 fragToLight = worldPos - u_LightPosInWorldSpace;
        float ligthDst = length(fragToLight) / lightFar;
        fragToLight = normalize(fragToLight) + sampleData * 0.02;
        float closestDepth = texture(u_RSMDepthTexture, fragToLight).r;

        float bias = 0.01;
        sum += abs(closestDepth - ligthDst) > bias ? 0.0 : 1.0;
    }
    return sum / 32.0;
}


vec3 calcVPLIrradiance(vec3 vplFlux, vec3 vplNormal, vec3 vplPos, vec3 fragPos, vec3 fragNormal, float weight){
    vec3 vplToFrag = normalize(fragPos - vplPos);

    float vplCos  = max(dot(vplNormal,  vplToFrag), 0.0);
    float fragCos = max(dot(fragNormal, -vplToFrag), 0.0);

    return vplFlux * vplCos * fragCos * weight;
}

vec3 calcIndirectIllumination(vec3 fragPos, vec3 fragNormal, vec3 fragAlbedo){
    vec3 indirect = vec3(0.0);

    for (int i = 0; i < u_VPLNum; i++){
        vec3 sampleData = u_VPLsSampleCoordsAndWeights[i].xyz;
        vec3 dir = normalize(fragPos - u_LightPosInWorldSpace);
        vec3 bias_dir = dir + sampleData * 0.1f;

        vec3 vplFlux   = texture(u_RSMFluxTexture, bias_dir).xyz;
        vec3 vplNormal = normalize(texture(u_RSMNormalTexture, bias_dir).xyz);
        vec3 vplPos    = texture(u_RSMPositionTexture, bias_dir).xyz;

        indirect += calcVPLIrradiance(vplFlux, vplNormal, vplPos, fragPos, fragNormal, 1.0);
    }

    indirect *= fragAlbedo;
    indirect /= float(u_VPLNum);
    indirect *= 10.0;

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

    // =========================
    // Direct Illumination
    // =========================
    vec3 ambient = fragAlbedo * 0.1;
    vec3 lightDir = normalize(fragPos - u_LightPosInWorldSpace);
    float NdotL = max(dot(fragNormal, -lightDir), 0.0);
    vec3 diffuse = fragAlbedo * NdotL;

    float shadow = calcShadow(fragPos);
    vec3 directIllumination = ambient;
    directIllumination += diffuse * shadow;

    // =========================
    // Indirect Illumination - RSM
    // =========================
    vec3 indirectIllumination = vec3(0.0);

    if (RTX > 0){
        indirectIllumination = calcIndirectIllumination(fragPos,fragNormal, fragAlbedo);
    }

    vec3 result = directIllumination + indirectIllumination;
    fragColor = vec4(result, 1.0);
}