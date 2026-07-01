#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 VP;

uniform sampler2D albedoTex;
uniform sampler2D normalTex;
uniform sampler2D worldPosTex;
uniform sampler2D depthTex;

uniform vec3 cameraPos;
uniform float cameraFar;
in vec3 worldVertex;

layout(location = 0) out vec4 fragColor;

vec3 Reflect(vec3 r, vec3 n) {
    return r - 2.0 * dot(r, n) * n;
}

vec3 worldSpaceToScreenSpace(vec3 worldPos){
    vec4 NDC = VP * vec4(worldPos, 1.0);
    vec3 screenPos =  NDC.xyz / NDC.w;
    screenPos = screenPos * 0.5 + 0.5;
    return screenPos;
}

bool IsOutSideScreen(vec3 pos){
    if(pos.x < 0 || pos.x > 1 || pos.y < 0 ||pos.y > 1 || pos.z < 0 || pos.z > 1) return true;
    return false;
}

void main() {
    vec3 screenPos = worldSpaceToScreenSpace(worldVertex);
    vec3 albedo = texture(albedoTex, screenPos.xy).rgb;
    vec3 normal = normalize(texture(normalTex, screenPos.xy).rgb);
    vec3 worldPos = texture(worldPosTex, screenPos.xy).rgb;
    vec3 depth = texture(depthTex, screenPos.xy).rgb * cameraFar;

    vec3 camPosToWorldPos = normalize(cameraPos - worldPos);
    vec3 dir = Reflect(-camPosToWorldPos, normal);
    float stepSize = 0.1;

    for(float step = 1 ; step< 100 ; step++){
        vec3 pos = worldVertex + dir *  step;
        vec3 screenStepPos = worldSpaceToScreenSpace(pos);
        if(IsOutSideScreen(screenPos)) break;
        float depth = texture(depthTex, screenStepPos.xy).r * cameraFar;
        if(depth <= 0) break;
        float reelDepth = length(cameraPos - pos);
        if(reelDepth - depth > 0.1){
            vec3 albedo = texture(albedoTex, screenStepPos.xy).rgb;
            fragColor = vec4(albedo, 1.0);
            return;
        }
    }

    fragColor = vec4(albedo , 1.0);
}