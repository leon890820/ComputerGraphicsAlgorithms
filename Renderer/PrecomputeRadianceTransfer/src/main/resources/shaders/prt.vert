#version 330

uniform mat4 MVP;
uniform mat4 modelMatrix;
uniform vec3 lightSH[9];

layout(location = 0) in vec3 aVertexPosition;
layout(location = 2) in vec2 aTexCoordPosition;
layout(location = 6) in vec3 aPRTCoeff0;
layout(location = 7) in vec3 aPRTCoeff1;
layout(location = 8) in vec3 aPRTCoeff2;

out vec2 texCoord;
out vec3 prtColor;

void main() {
    float transfer[9] = float[9](
        aPRTCoeff0.x, aPRTCoeff0.y, aPRTCoeff0.z,
        aPRTCoeff1.x, aPRTCoeff1.y, aPRTCoeff1.z,
        aPRTCoeff2.x, aPRTCoeff2.y, aPRTCoeff2.z
    );

    vec3 lighting = vec3(0.0);
    for (int i = 0; i < 9; i++) {
        lighting += lightSH[i] * transfer[i];
    }

    prtColor = max(lighting / 3.14159265, vec3(0.0));
    texCoord = aTexCoordPosition;
    gl_Position = MVP * vec4(aVertexPosition, 1.0);
}
