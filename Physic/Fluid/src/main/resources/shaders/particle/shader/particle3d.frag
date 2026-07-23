#version 430 core

in vec3 vNormal;
in vec3 vWorldPos;
in float vSpeedT;

uniform vec3 uColour;
uniform vec3 uRimColour;
uniform vec3 uLightDir;
uniform vec3 uCameraPosition;

out vec4 FragColor;

void main() {
    vec3 n = normalize(vNormal);
    vec3 viewDir = normalize(uCameraPosition - vWorldPos);
    vec3 lightDir = normalize(uLightDir);

    float diffuse = 0.35 + 0.65 * max(dot(lightDir, n), 0.0);
    float fresnel = pow(1.0 - max(dot(n, viewDir), 0.0), 2.2);
    vec3 speedTint = mix(vec3(0.9), vec3(1.08), vSpeedT);
    vec3 color = uColour * diffuse * speedTint + uRimColour * fresnel * 0.65;

    FragColor = vec4(color, 1.0);
}
