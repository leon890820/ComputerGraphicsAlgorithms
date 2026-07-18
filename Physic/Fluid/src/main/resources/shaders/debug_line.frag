#version 330 core

uniform vec3 uColour;

out vec4 FragColor;

void main() {
    FragColor = vec4(uColour, 1.0);
}
