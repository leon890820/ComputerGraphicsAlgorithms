#version 430

struct Triangle {
    vec4 pointA;
    vec4 pointB;
    vec4 pointC;
};

layout(std430, binding = 0) readonly buffer Triangles {
    Triangle TriangleData[];
};

uniform mat4 viewProjection;
uniform sampler3D densityTexture;
uniform vec3 boundsCenter;
uniform vec3 boundsSize;

out vec3 worldNormal;
out vec3 worldVertex;

vec3 getTrianglePoint(Triangle tri, int cornerIndex) {
    if (cornerIndex == 0) {
        return tri.pointA.xyz;
    }

    if (cornerIndex == 1) {
        return tri.pointB.xyz;
    }

    return tri.pointC.xyz;
}

vec3 sampleDensityGradient(vec3 worldPosition) {
    vec3 boundsMin = boundsCenter - boundsSize * 0.5;
    vec3 uvw = clamp((worldPosition - boundsMin) / boundsSize, 0.0, 1.0);
    vec3 texel = 1.0 / vec3(textureSize(densityTexture, 0));

    float dx = textureLod(densityTexture, clamp(uvw + vec3(texel.x, 0.0, 0.0), 0.0, 1.0), 0.0).x
             - textureLod(densityTexture, clamp(uvw - vec3(texel.x, 0.0, 0.0), 0.0, 1.0), 0.0).x;
    float dy = textureLod(densityTexture, clamp(uvw + vec3(0.0, texel.y, 0.0), 0.0, 1.0), 0.0).x
             - textureLod(densityTexture, clamp(uvw - vec3(0.0, texel.y, 0.0), 0.0, 1.0), 0.0).x;
    float dz = textureLod(densityTexture, clamp(uvw + vec3(0.0, 0.0, texel.z), 0.0, 1.0), 0.0).x
             - textureLod(densityTexture, clamp(uvw - vec3(0.0, 0.0, texel.z), 0.0, 1.0), 0.0).x;

    vec3 gradient = vec3(dx, dy, dz);
    if (dot(gradient, gradient) < 0.000001) {
        return vec3(0.0, 1.0, 0.0);
    }

    return normalize(gradient);
}

void main() {
    int triangleIndex = gl_VertexID / 3;
    int cornerIndex = gl_VertexID - triangleIndex * 3;
    Triangle tri = TriangleData[triangleIndex];
    vec3 point = getTrianglePoint(tri, cornerIndex);

    worldVertex = point;
    worldNormal = sampleDensityGradient(point);
    gl_Position = viewProjection * vec4(point, 1.0);
}
