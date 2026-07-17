#version 430 core

in vec3 vNormal;
in float vSpeedT;

uniform vec3 uColour;
uniform vec3 uLightDir;

out vec4 FragColor;

void main() {
    float shading = clamp(dot(normalize(uLightDir), normalize(vNormal)), 0.0, 1.0);
    shading = (shading + 0.6) / 1.4;

    vec3 speedTint = mix(uColour, vec3(1.0, 0.72, 0.25), vSpeedT);
    FragColor = vec4(speedTint * shading, 1.0);
}