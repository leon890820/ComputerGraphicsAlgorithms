#version 330

uniform vec3 fluidColor;
uniform vec3 rimColor;
uniform vec3 cameraPosition;
uniform sampler2D sceneColorTexture;
uniform sampler2D sceneDepthTexture;
uniform vec2 screenSize;
uniform float cameraNear;
uniform float cameraFar;
uniform float rimPower;
uniform float rimStrength;
uniform float specularStrength;
uniform float alpha;
uniform float refractionStrength;
uniform float depthFadeMultiplier;
uniform int hasSceneTextures;

in vec3 worldNormal;
in vec3 worldVertex;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragWorldPos;

float linearEyeDepth(float rawDepth) {
    float z = rawDepth * 2.0 - 1.0;
    return (2.0 * cameraNear * cameraFar) / (cameraFar + cameraNear - z * (cameraFar - cameraNear));
}

void main() {
    vec3 n = normalize(worldNormal);
    vec3 viewDir = normalize(cameraPosition - worldVertex);
    n = faceforward(n, -viewDir, n);

    vec3 lightDir = normalize(vec3(0.35, 0.8, 0.45));
    vec3 halfDir = normalize(lightDir + viewDir);

    float diffuse = 0.35 + 0.65 * max(dot(n, lightDir), 0.0);
    float fresnel = pow(1.0 - max(dot(n, viewDir), 0.0), rimPower);
    float specular = pow(max(dot(n, halfDir), 0.0), 64.0) * specularStrength;

    vec3 surface = fluidColor * diffuse;
    vec3 emission = rimColor * fresnel * rimStrength;
    vec3 highlight = vec3(specular);
    float outAlpha = alpha;

    if (hasSceneTextures == 1) {
        vec2 screenUv = gl_FragCoord.xy / max(screenSize, vec2(1.0));
        vec2 refractOffset = n.xy * refractionStrength * (0.25 + fresnel);
        vec3 sceneColor = texture(sceneColorTexture, clamp(screenUv + refractOffset, 0.0, 1.0)).rgb;
        float sceneDepth = texture(sceneDepthTexture, screenUv).r;
        float fluidDepth = gl_FragCoord.z;
        float sceneEyeDepth = linearEyeDepth(sceneDepth);
        float fluidEyeDepth = linearEyeDepth(fluidDepth);
        float hasSceneHit = 1.0 - step(0.9999, sceneDepth);
        float thickness = max(sceneEyeDepth - fluidEyeDepth, 0.0) * hasSceneHit;
        float depthFade = 1.0 - exp(-thickness * depthFadeMultiplier);
        depthFade = max(depthFade, 0.12 * (1.0 - hasSceneHit));

        vec3 tintedRefraction = mix(sceneColor, sceneColor * fluidColor, 0.55);
        surface = mix(tintedRefraction, surface, 0.35 + depthFade * 0.45);
        emission += rimColor * depthFade * 0.25;
        outAlpha = clamp(alpha * (0.55 + depthFade * 0.45) + fresnel * 0.08, 0.0, 1.0);
    }

    fragColor = vec4(surface + emission + highlight, outAlpha);
    fragNormal = vec4(n, 1.0);
    fragWorldPos = vec4(worldVertex, 1.0);
}
