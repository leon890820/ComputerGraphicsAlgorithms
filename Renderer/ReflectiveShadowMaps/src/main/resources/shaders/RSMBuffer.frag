#version 330
#ifdef GL_ES
precision mediump float;
#endif


uniform sampler2D tex;
uniform vec3 u_LightColor = vec3(1);
uniform vec3 lightPos;
uniform float lightFar;

in vec3 worldPosition;
in vec3 worldNormal;
in vec2 texCoord;

layout(location = 0) out vec4 fragFlux;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragPosition;

void main() {  
    vec3 Albedo = texture(tex, texCoord).rgb;
    fragFlux = vec4(u_LightColor * Albedo,1.0);
    fragNormal = vec4(worldNormal,1.0);
    fragPosition = vec4(worldPosition,1.0);

    float lightDistance = length(worldPosition - lightPos);
    lightDistance = lightDistance / lightFar;
    gl_FragDepth = lightDistance;
}