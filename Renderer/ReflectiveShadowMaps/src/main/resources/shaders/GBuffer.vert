#version 330

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectMatrix;

layout(location = 0) in vec3 aVertexPosition;
layout(location = 1) in vec3 aNormalPosition;
layout(location = 2) in vec2 aTexCoordPosition;

out vec3 worldPosition;
out vec3 worldNormal;
out vec2 texCoord;

void main() {
  vec4 FragPosInViewSpace = modelMatrix * vec4(aVertexPosition, 1.0f);
  gl_Position = projectMatrix * viewMatrix * FragPosInViewSpace;

  worldNormal = normalize(mat3(transpose(inverse(modelMatrix))) * aNormalPosition);
  worldPosition = vec3(FragPosInViewSpace);
  texCoord = aTexCoordPosition;
}