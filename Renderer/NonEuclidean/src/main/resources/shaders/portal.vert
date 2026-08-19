#version 330 core

layout(location = 0) in vec3 aVertexPosition;
layout(location = 2) in vec2 aTexCoordPosition;

uniform mat4 MVP;

out vec4 ex_uv;

void main(void) {
    gl_Position = MVP * vec4(aVertexPosition, 1.0);
    ex_uv = gl_Position;
}
