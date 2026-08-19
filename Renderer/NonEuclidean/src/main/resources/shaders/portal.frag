#version 330 core

uniform sampler2D tex;

in vec4 ex_uv;

out vec4 fragColor;

void main(void) {
    vec2 uv = ex_uv.xy / ex_uv.w;
    uv = uv * 0.5 + 0.5;
    fragColor = vec4(texture(tex, uv).rgb, 1.0);
}
