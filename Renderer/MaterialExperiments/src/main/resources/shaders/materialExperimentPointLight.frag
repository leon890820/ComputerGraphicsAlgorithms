#version 330
#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D albedo;
uniform sampler2D worldPos;
uniform sampler2D worldNormal;
uniform samplerCube shadowCubeMap;
uniform sampler3D ditherTex;
uniform sampler2D ditherRampTex;

uniform vec3 light_pos;
uniform vec3 light_color;
uniform vec3 view_pos;
uniform vec3 ditherTexSize;
uniform vec3 ditherPaperColor;
uniform vec3 ditherInkColor;
uniform float lightFar;
uniform float ditherScale;
uniform float ditherSizeVariability;
uniform float ditherContrast;
uniform float ditherStretchSmoothness;
uniform float ditherInputExposure;
uniform float ditherInputOffset;
uniform float ditherStrength;
uniform float ditherAntiAlias;
uniform float ditherMoireFadeStart;
uniform float ditherMoireFadeEnd;
uniform int ditherMode;
uniform int compareWipeEnabled;
uniform int compareWipeDirection;
uniform float compareWipePosition;
uniform float compareWipeEdge;
uniform int useDitherTex;
uniform int useDitherRampTex;

in vec2 texcoord;

layout(location = 0) out vec4 fragColor;

float saturate(float v)
{
    return clamp(v, 0.0, 1.0);
}

vec3 saturate(vec3 v)
{
    return clamp(v, vec3(0.0), vec3(1.0));
}

float getGrayscale(vec3 color)
{
    return saturate(dot(color, vec3(0.299, 0.587, 0.114)));
}

float ShadowCalculationPoint(vec3 fragPos, vec3 N, vec3 L)
{
    vec3 fragToLight = fragPos - light_pos;
    float currentDepth = length(fragToLight);

    float closestDepth = texture(shadowCubeMap, fragToLight).r * lightFar;
    if (closestDepth <= 0.0001) {
        return 0.0;
    }

    float bias = max(2.0 * (1.0 - dot(N, L)), 0.75);
    return currentDepth - bias > closestDepth ? 0.7 : 0.0;
}

vec3 evaluateBlinnPhong(
    vec3 baseColor,
    vec3 worldVertex,
    vec3 normal,
    vec3 lightDir,
    vec3 viewDir,
    float lightDistance,
    float shadow
) {
    vec3 halfDir = normalize(lightDir + viewDir);

    vec3 ambient = baseColor * 0.3;
    vec3 diffuse = baseColor * 0.7 * light_color * max(0.0, dot(normal, lightDir));
    vec3 specular = 0.3 * light_color * pow(max(0.0, dot(normal, halfDir)), 64.0);

    return ambient + (1.0 - shadow) * (diffuse + specular);
}

vec2 getSurfaceDitherUv(vec3 worldVertex, vec3 normal)
{
    vec3 an = abs(normal);

    if (an.y >= an.x && an.y >= an.z) {
        return worldVertex.xz;
    }

    if (an.x >= an.y && an.x >= an.z) {
        return worldVertex.zy;
    }

    return worldVertex.xy;
}

float getDither3D(vec2 uvDitherTex, vec2 dx, vec2 dy, float brightness)
{
    if (useDitherTex == 0) {
        return brightness;
    }

    float xRes = max(ditherTexSize.x, 1.0);
    float dotsPerSide = xRes / 16.0;
    float dotsTotal = max(ditherTexSize.z, 1.0);
    float invZres = 1.0 / dotsTotal;

    float invXres = 1.0 / xRes;
    vec2 lookup = vec2(0.5 * invXres + (1.0 - invXres) * brightness, 0.5);
    float brightnessCurve = useDitherRampTex == 1
            ? texture(ditherRampTex, lookup).r
            : brightness;

    mat2 matr = mat2(dx, dy);
    vec4 vectorized = vec4(dx, dy);
    float Q = dot(vectorized, vectorized);
    float R = determinant(matr);
    float discriminantSqr = max(0.0, Q * Q - 4.0 * R * R);
    float discriminant = sqrt(discriminantSqr);

    vec2 freq = sqrt(max(vec2(Q + discriminant, Q - discriminant) * 0.5, vec2(0.0)));
    float maxFreq = max(freq.x, 0.000001);
    float minFreq = max(freq.y, 0.000001);

    float spacing = minFreq;
    float scaleExp = exp2(ditherScale);
    spacing *= scaleExp;
    spacing *= dotsPerSide * 0.125;

    float sizeVariability = saturate(ditherSizeVariability);
    float brightnessSpacingMultiplier = pow(brightnessCurve * 2.0 + 0.001, -(1.0 - sizeVariability));
    spacing *= brightnessSpacingMultiplier;

    float spacingLog = log2(max(spacing, 0.000001));
    float patternScaleLevel = floor(spacingLog);
    float f = spacingLog - patternScaleLevel;

    vec2 uv = uvDitherTex / exp2(patternScaleLevel);

    float subLayer = mix(0.25 * dotsTotal, dotsTotal, 1.0 - f);
    subLayer = (subLayer - 0.5) * invZres;

    float pattern = texture(ditherTex, vec3(uv, subLayer)).r;

    float contrast = ditherContrast * scaleExp * brightnessSpacingMultiplier * 0.1;
    contrast *= pow(minFreq / maxFreq, ditherStretchSmoothness);

    float fadeEnd = max(ditherMoireFadeEnd, ditherMoireFadeStart + 0.001);
    float farAliasFade = smoothstep(ditherMoireFadeStart, fadeEnd, spacing);
    float tinyAliasFade = 1.0 - smoothstep(0.35, 0.75, spacing);
    float aliasFade = max(farAliasFade, tinyAliasFade) * saturate(ditherAntiAlias);
    float effectiveContrast = contrast / (1.0 + aliasFade * 8.0);

    float baseVal = mix(0.5, brightness, saturate(1.05 / (1.0 + effectiveContrast)));
    float threshold = 1.0 - brightnessCurve;
    float bw = saturate((pattern - threshold) * effectiveContrast + baseVal);
    bw = mix(bw, brightness, aliasFade);

    return bw;
}

vec3 applyMaterialEffect(
    vec3 litColor,
    vec3 baseColor,
    vec3 worldVertex,
    vec3 normal,
    vec3 lightDir,
    vec3 viewDir,
    vec2 uv,
    float shadow
) {
    if (ditherStrength <= 0.0) {
        return litColor;
    }

    vec3 adjustedColor = saturate(litColor * ditherInputExposure + vec3(ditherInputOffset));

    vec2 uvDitherTex = getSurfaceDitherUv(worldVertex, normal);
    vec2 dx = dFdx(uvDitherTex);
    vec2 dy = dFdy(uvDitherTex);

    float brightness = getGrayscale(adjustedColor);
    float bw = getDither3D(uvDitherTex, dx, dy, brightness);
    vec3 bwColor = mix(ditherInkColor, ditherPaperColor, bw);

    if (compareWipeEnabled == 1) {
        float edge = max(compareWipeEdge, 0.0001);
        float x = texcoord.x;
        float wipeMask = compareWipeDirection >= 0
                ? 1.0 - smoothstep(compareWipePosition - edge, compareWipePosition + edge, x)
                : smoothstep(compareWipePosition - edge, compareWipePosition + edge, x);
        float line = 1.0 - smoothstep(0.0, edge, abs(x - compareWipePosition));
        vec3 color = mix(litColor, bwColor, wipeMask);
        return mix(color, vec3(1.0), line * 0.75);
    }

    if (ditherMode == 0) {
        return litColor;
    }

    vec3 ditherColor;
    if (ditherMode == 2) {
        vec3 rgb;
        rgb.r = getDither3D(uvDitherTex, dx, dy, adjustedColor.r);
        rgb.g = getDither3D(uvDitherTex, dx, dy, adjustedColor.g);
        rgb.b = getDither3D(uvDitherTex, dx, dy, adjustedColor.b);
        ditherColor = mix(ditherInkColor, ditherPaperColor, rgb);
    } else {
        ditherColor = bwColor;
    }

    return mix(litColor, ditherColor, saturate(ditherStrength));
}

void main() {
    vec3 baseColor = texture(albedo, texcoord).rgb;
    vec3 worldVertex = texture(worldPos, texcoord).rgb;
    vec3 N = normalize(texture(worldNormal, texcoord).rgb);

    vec3 lightVec = light_pos - worldVertex;
    float lightDistance = length(lightVec);
    vec3 L = lightVec / max(lightDistance, 0.0001);
    vec3 V = normalize(view_pos - worldVertex);

    float shadow = ShadowCalculationPoint(worldVertex, N, L);
    vec3 litColor = evaluateBlinnPhong(baseColor, worldVertex, N, L, V, lightDistance, shadow);
    vec3 finalColor = applyMaterialEffect(litColor, baseColor, worldVertex, N, L, V, texcoord, shadow);

    fragColor = vec4(finalColor, 1.0);
}


