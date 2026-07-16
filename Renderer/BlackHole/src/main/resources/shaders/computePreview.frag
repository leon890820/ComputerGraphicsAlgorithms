#version 330

uniform sampler2D screenTexture;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

void main() {
    fragColor = texture(screenTexture, texcoord);
}
