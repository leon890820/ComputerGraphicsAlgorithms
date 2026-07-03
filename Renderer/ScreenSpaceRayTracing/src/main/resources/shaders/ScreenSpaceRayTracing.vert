#version 330

uniform mat4 modelMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;

layout(location = 0) in vec3 aVertexPosition;
layout(location = 1) in vec3 aNormalPosition;
layout(location = 2) in vec2 aTexCoordPosition;
layout(location = 3) in vec3 aTangentPosition;

out vec3 worldVertex;
out vec3 worldNormal;
out vec3 worldTangent;
out vec2 texCoord;

void main() {
    vec4 worldPos = modelMatrix * vec4(aVertexPosition, 1.0);
    worldVertex = worldPos.xyz;
    worldNormal = normalize((modelMatrix * vec4(aNormalPosition, 0.0)).xyz);
    worldTangent = normalize((modelMatrix * vec4(aTangentPosition, 0.0)).xyz);
    texCoord = aTexCoordPosition;
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * worldPos;
}
