#version 430 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aUV;

layout(std430, binding = 0) readonly buffer Positions {
    vec4 PositionsData[];
};

layout(std430, binding = 1) readonly buffer Velocities {
    vec4 VelocitiesData[];
};

uniform mat4 uMVP;
uniform mat4 uLocalToWorld;
uniform float uScale;
uniform float uVelocityMax;

out vec3 vNormal;
out vec3 vWorldPos;
out float vSpeedT;

void main() {
    int id = gl_InstanceID;

    vec3 center = PositionsData[id].xyz;
    vec3 velocity = VelocitiesData[id].xyz;
    vec3 localPos = center + aPosition * uScale;

    gl_Position = uMVP * vec4(localPos, 1.0);

    vNormal = normalize((uLocalToWorld * vec4(aNormal, 0.0)).xyz);
    vWorldPos = localPos;
    vSpeedT = clamp(length(velocity) / max(uVelocityMax, 0.0001), 0.0, 1.0);
}
