#version 330

uniform mat4 MVP;
uniform mat4 modelMatrix;
uniform int useSkinning;
uniform mat4 boneMatrices[100];

layout(location = 0) in vec3 aVertexPosition;
layout(location = 1) in vec3 aNormalPosition;
layout(location = 2) in vec2 aTexCoordPosition;
layout(location = 3) in vec3 aTangentPosition;
layout(location = 4) in ivec4 aBoneIds;
layout(location = 5) in vec4 aBoneWeights;

out vec3 worldNormal;
out vec3 worldTangent;
out vec3 worldVertex;
out vec2 texCoord;

void main() {
    mat4 skinMatrix = mat4(1.0);

    if (useSkinning == 1) {
        skinMatrix = boneMatrices[aBoneIds.x] * aBoneWeights.x;
        skinMatrix += boneMatrices[aBoneIds.y] * aBoneWeights.y;
        skinMatrix += boneMatrices[aBoneIds.z] * aBoneWeights.z;
        skinMatrix += boneMatrices[aBoneIds.w] * aBoneWeights.w;
    }

    vec4 localPos = skinMatrix * vec4(aVertexPosition, 1.0);
    vec4 localNormal = skinMatrix * vec4(aNormalPosition, 0.0);
    vec4 localTangent = skinMatrix * vec4(aTangentPosition, 0.0);
    vec4 worldPos = modelMatrix * localPos;

    gl_Position = MVP * localPos;

    worldNormal = normalize(modelMatrix * localNormal).xyz;
    worldTangent = normalize(modelMatrix * localTangent).xyz;
    worldVertex = worldPos.xyz;
    texCoord = aTexCoordPosition;
}
