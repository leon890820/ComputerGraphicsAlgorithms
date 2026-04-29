#version 440
#ifdef GL_ES
precision mediump float;
#endif

#define VPL_NUM 32

uniform sampler2D u_AlbedoTexture;
uniform sampler2D u_NormalTexture;
uniform sampler2D u_PositionTexture;
uniform sampler2D u_RSMFluxTexture;
uniform sampler2D u_RSMNormalTexture;		
uniform sampler2D u_RSMPositionTexture;
uniform sampler2D u_RSMDepthTexture;

uniform mat4  u_LightVPMatrix;
uniform float u_MaxSampleRadius;
uniform int   u_RSMSize;
uniform int   u_VPLNum;
uniform vec3  u_LightDirInWorldSpace;
uniform int RTX;

in vec2 texCoord;



layout (std430,binding = 0) buffer VPLsSampleCoordsAndWeights{
    vec4 u_VPLsSampleCoordsAndWeights[VPL_NUM];
};


layout(location = 0) out vec4 fragColor;

vec3 calcVPLIrradiance(vec3 vVPLFlux, vec3 vVPLNormal, vec3 vVPLPos, vec3 vFragPos, vec3 vFragNormal, float vWeight)
{
	vec3 VPL2Frag = normalize(vFragPos - vVPLPos);
	return vVPLFlux * max(dot(vVPLNormal, VPL2Frag), 0) * max(dot(vFragNormal, -VPL2Frag), 0) * vWeight;
}

float toLinear(float depth){
    
    float near = 0.01;
    float far  = 3000.0;

    float linearDepth = (2.0 * near * far) / 
                            (far + near - depth * (far - near));

        // normalize 顯示
    linearDepth = linearDepth / far;
    return linearDepth;

}

void main()
{
    vec3 FragViewNormal = normalize(texture(u_NormalTexture, texCoord).xyz);
    vec3 FragAlbedo     = texture(u_AlbedoTexture, texCoord).xyz;
    vec3 FragViewPos    = texture(u_PositionTexture, texCoord).xyz;

    // view space -> light clip space
    vec4 FragPosInLightClip = u_LightVPMatrix * vec4(FragViewPos, 1.0);

    // 透視除法，得到 NDC [-1, 1]
    vec3 FragPosInLightNDC = FragPosInLightClip.xyz / FragPosInLightClip.w;

    // 再轉成 [0, 1]，給 texture sample / depth compare 用
    vec3 FragPosInLightTex = FragPosInLightNDC * 0.5 + 0.5;
    vec2 FragNDCPos4Light  = FragPosInLightTex.xy;

    float RSMTexelSize = 1.0 / float(u_RSMSize);

    // =========================
    // Shadow
    // =========================
    float shadow_sum = 0.0;

    bool outsideLightFrustum =
        FragPosInLightTex.x < 0.0 || FragPosInLightTex.x > 1.0 ||
        FragPosInLightTex.y < 0.0 || FragPosInLightTex.y > 1.0 ||
        FragPosInLightTex.z < 0.0 || FragPosInLightTex.z > 1.0;

    if (!outsideLightFrustum){        
        float currentDepth = FragPosInLightTex.z;        
        float currentLinearDepth = toLinear(currentDepth);

        for(int i = 0; i < VPL_NUM; i++){            
            vec2 bias_coord = u_VPLsSampleCoordsAndWeights[i].xy * RSMTexelSize * 10 + FragPosInLightTex.xy;
            float closestDepth = texture(u_RSMDepthTexture, bias_coord).r;
            float linearDepth = toLinear(closestDepth);
            float shadow = (currentLinearDepth - linearDepth  > 0.03) ? 0.0 : 1.0;
            shadow_sum += shadow;
            
        }

        shadow_sum /= VPL_NUM;
    }

    // =========================
    // Direct Illumination
    // =========================
    vec3 ambient = vec3(0.1) * FragAlbedo;
    float NdotL = max(dot(-u_LightDirInWorldSpace, FragViewNormal), 0.0);
    vec3 diffuse = FragAlbedo * NdotL;

    vec3 DirectIllumination;
    if (outsideLightFrustum)
        DirectIllumination = ambient;
    else
        DirectIllumination = ambient + shadow_sum * diffuse;

    // =========================
    // Indirect Illumination (RSM)
    // =========================
    vec3 IndirectIllumination = vec3(0.0);

    for (int i = 0; i < u_VPLNum; i++)
    {
        vec3 VPLSampleCoordAndWeight = u_VPLsSampleCoordsAndWeights[i].xyz;
        vec2 VPLSamplePos = FragNDCPos4Light + u_MaxSampleRadius * VPLSampleCoordAndWeight.xy * RSMTexelSize;

        if (VPLSamplePos.x < 0.0 || VPLSamplePos.x > 1.0 ||
            VPLSamplePos.y < 0.0 || VPLSamplePos.y > 1.0)
        {
            continue;
        }

        vec3 VPLFlux                = texture(u_RSMFluxTexture, VPLSamplePos).xyz;
        vec3 VPLNormalInViewSpace   = normalize(texture(u_RSMNormalTexture, VPLSamplePos).xyz);
        vec3 VPLPositionInViewSpace = texture(u_RSMPositionTexture, VPLSamplePos).xyz;

        IndirectIllumination += calcVPLIrradiance(
            VPLFlux,
            VPLNormalInViewSpace,
            VPLPositionInViewSpace,
            FragViewPos,
            FragViewNormal,
            VPLSampleCoordAndWeight.z
        );
    }

    IndirectIllumination *= FragAlbedo;

    if (RTX < 0.5)
        IndirectIllumination = vec3(0.0);

    vec3 Result = DirectIllumination + (IndirectIllumination / float(u_VPLNum)) * 20.0;
    fragColor = vec4(Result, 1.0);
}