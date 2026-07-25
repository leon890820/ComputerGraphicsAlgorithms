#version 430

layout(location = 0) in vec3 aVertexPosition;
layout(location = 2) in vec2 aTexCoordPosition;

out vec2 texcoord;

void main() {
    gl_Position = vec4(aVertexPosition, 1.0);
    texcoord = aTexCoordPosition;
}
