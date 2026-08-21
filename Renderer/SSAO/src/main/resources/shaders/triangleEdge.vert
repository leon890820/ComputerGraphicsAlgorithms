#version 330

uniform mat4 MVP;
uniform int useSkinning;
uniform mat4 boneMatrices[100];

layout(location = 0) in vec3 aVertexPosition;
layout(location = 4) in ivec4 aBoneIds;
layout(location = 5) in vec4 aBoneWeights;

void main() {
    mat4 skinMatrix = mat4(1.0);

    if (useSkinning == 1) {
        skinMatrix = boneMatrices[aBoneIds.x] * aBoneWeights.x;
        skinMatrix += boneMatrices[aBoneIds.y] * aBoneWeights.y;
        skinMatrix += boneMatrices[aBoneIds.z] * aBoneWeights.z;
        skinMatrix += boneMatrices[aBoneIds.w] * aBoneWeights.w;
    }

    gl_Position = MVP * skinMatrix * vec4(aVertexPosition, 1.0);
}
