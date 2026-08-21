#version 330

uniform sampler2D ssaoInput;
uniform vec2 texelSize;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

void main() {
    float result = 0.0;

    for (int x = -2; x < 2; x++) {
        for (int y = -2; y < 2; y++) {
            vec2 offset = vec2(float(x), float(y)) * texelSize;
            result += texture(ssaoInput, texcoord + offset).r;
        }
    }

    result /= 16.0;
    fragColor = vec4(vec3(result), 1.0);
}
