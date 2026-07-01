#version 330

uniform mat4 VP;
uniform mat4 modelMatrix;

layout(location = 0) in vec3 aVertexPosition;
layout(location = 1) in vec3 aNormalPosition;
layout(location = 2) in vec2 aTexCoordPosition;

out vec3 worldVertex;

void main() {
    vec4 worldPos = modelMatrix * vec4(aVertexPosition, 1.0);
    worldVertex = worldPos.xyz;
    gl_Position = VP * vec4(worldPos.xyz, 1.0);
}