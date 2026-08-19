#version 330 core

layout(location = 0) in vec3 aVertexPosition;
layout(location = 2) in vec2 aTexCoordPosition;

uniform mat4 mvp;
uniform mat4 mv;

out vec3 ex_normal;
out vec2 texcoord;

void main(void) {
    gl_Position = vec4(aVertexPosition.xy, 0.0, 1.0);
    vec3 eye_normal = normalize((mvp * gl_Position).xyz);
    ex_normal = normalize((mv * vec4(eye_normal, 0.0)).xyz);
    texcoord = aTexCoordPosition;
}
