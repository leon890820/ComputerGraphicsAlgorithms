#version 330

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectMatrix;
uniform int useSkinning;
uniform mat4 boneMatrices[100];

layout(location = 0) in vec3 aVertexPosition;
layout(location = 1) in vec3 aNormalPosition;
layout(location = 2) in vec2 aTexCoordPosition;
layout(location = 4) in ivec4 aBoneIds;
layout(location = 5) in vec4 aBoneWeights;

out vec3 worldPosition;
out vec3 worldNormal;
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
  vec4 worldPos = modelMatrix * localPos;
  gl_Position = projectMatrix * viewMatrix * worldPos;

  worldNormal = normalize(mat3(transpose(inverse(modelMatrix))) * localNormal.xyz);
  worldPosition = worldPos.xyz;
  texCoord = aTexCoordPosition;
}
