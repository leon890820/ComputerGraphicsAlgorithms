#version 330 core

#define LIGHT vec3(0.36, 0.80, 0.48)
#define SUN_SIZE 0.002
#define SUN_SHARPNESS 1.0

uniform sampler2D depthTex;
uniform int useDepthTest;

in vec3 ex_normal;
in vec2 texcoord;

out vec4 fragColor;

void main(void) {
    if (useDepthTest == 1) {
        float sceneDepth = texture(depthTex, texcoord).r;
        if (sceneDepth < 0.999999) {
            discard;
        }
    }

    vec3 n = normalize(ex_normal);

    float h = (1.0 - n.y) * (1.0 - n.y) * 0.5;
    vec3 sky = vec3(0.2 + h, 0.5 + h, 1.0);

    float s = dot(n, LIGHT) - 1.0 + SUN_SIZE;
    float sun = min(exp(s * SUN_SHARPNESS / SUN_SIZE), 1.0);

    fragColor = vec4(max(sky, vec3(sun)), 1.0);
}
