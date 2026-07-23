#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform vec3 light_dir;
uniform vec3 ambient_light;
uniform vec3 light_color;
uniform vec3 view_pos;

in vec3 worldNormal;
in vec3 worldVertex;
in vec2 texCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragNormal;
layout(location = 2) out vec4 fragWorldPos;

float gridLine(vec2 coord, float scale, float thickness) {
    vec2 scaled = coord * scale;
    vec2 deriv = fwidth(scaled);
    vec2 grid = abs(fract(scaled - 0.5) - 0.5) / max(deriv, vec2(0.0001));
    float line = 1.0 - min(min(grid.x, grid.y), 1.0);
    return smoothstep(0.0, thickness, line);
}

void main() {
    vec2 floorCoord = worldVertex.xz;

    vec2 checkerCoord = floor(floorCoord * 2.0);
    float checker = mod(checkerCoord.x + checkerCoord.y, 2.0);
    vec3 baseColor = mix(vec3(0.58), vec3(0.72), checker);

    float minorGrid = gridLine(floorCoord, 4.0, 0.75);
    float majorGrid = gridLine(floorCoord, 1.0, 0.9);
    vec3 gridColor = mix(vec3(0.82), vec3(0.95), majorGrid);
    vec3 surfaceColor = mix(baseColor, gridColor, max(minorGrid * 0.45, majorGrid * 0.8));

    vec3 N = normalize(worldNormal);
    vec3 L = normalize(-light_dir);
    vec3 V = normalize(view_pos - worldVertex);
    vec3 H = normalize(L + V);

    vec3 ambient = surfaceColor * ambient_light;
    vec3 diffuse = surfaceColor * 0.65 * light_color * max(0.0, dot(N, L));
    vec3 specular = 0.12 * light_color * pow(max(0.0, dot(N, H)), 32.0);

    fragColor = vec4(ambient + diffuse + specular, 1.0);
    fragNormal = vec4(worldNormal, 1.0);
    fragWorldPos = vec4(worldVertex, 1.0);
}
