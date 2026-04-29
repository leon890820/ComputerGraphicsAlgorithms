#version 330
#ifdef GL_ES
precision mediump float;
#endif


uniform sampler2D albedo;
in vec2 texcoord;

layout(location = 0) out vec4 fragColor;



void main() {
  vec3 texture_color = texture(albedo, texcoord).rgb;
  fragColor = vec4(texture_color , 1.0);
}