#version 150
precision mediump float;
uniform float Time; //0 to 1
uniform float IridTimeSec;
uniform float IridLevel;
uniform float IridProgress;
uniform float IridDistortion;
uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;
in vec2 screenCoords;

out vec4 fragColor;

float random (in vec3 st) {
    return fract(sin(dot(st,
                         vec3(12.9898,78.233,4.1414)))*
                 43758.5453123);
}
float noise (in vec3 st) {
    vec3 i = floor(st);
    vec3 f = fract(st);

    float a = random(i);
    float b = random(i + vec3(1.0, 0.0, 0.0));
    float c = random(i + vec3(0.0, 1.0, 0.0));
    float d = random(i + vec3(1.0, 1.0, 0.0));
    float a1 = random(i + vec3(0.0, 0.0, 1.0));
    float b1 = random(i + vec3(1.0, 0.0, 1.0));
    float c1 = random(i + vec3(0.0, 1.0, 1.0));
    float d1 = random(i + vec3(1.0, 1.0, 1.0));

    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(
            mix(a, b, u.x),
            mix(c, d, u.x),
            u.y),
        mix(
            mix(a1, b1, u.x),
            mix(c1, d1, u.x),
            u.y),
        u.z);
}

void main() {
    float noiseScale1 = 1.0f + IridLevel/2.0f; //1 to 3
    float noiseScale2 = 1.5f + IridLevel/4.0f; //1.5 to 2.5

    float twopi = 6.2832f;
    float time = twopi*(IridTimeSec / 10.0f);
    float alpha = IridProgress > 0.5 ? 1 : 2*IridProgress;

    float primaryColor = 2*fract(IridTimeSec / 20.0f);
    primaryColor = twopi*alpha*((primaryColor > 1) ? 2 - primaryColor : primaryColor);
    if (fract(IridTimeSec / 40.0f) > 0.5) primaryColor = -primaryColor;

    float iTime = primaryColor + IridProgress*noiseScale2*noise(vec3(noiseScale1*screenCoords, 0.2*noiseScale2*time));

    float a = (1.0f - cos(iTime))/3.0f + cos(iTime);
    float b = (1.0f - cos(iTime))/3.0f - sin(iTime) / sqrt(3);
    float c = (1.0f - cos(iTime))/3.0f + sin(iTime) / sqrt(3);

    float warpScale = 0.1 * IridDistortion * IridProgress * IridLevel / 12;
    float edgeFactor = 1 - 2*min(min(texCoord.x, 1 - texCoord.x), min(texCoord.y, 1 - texCoord.y));
    float texOffsetX = noise(vec3(3*screenCoords, 0.5*(time + 10000))) - 0.5;
    float texOffsetY = noise(vec3(3*screenCoords, 0.5*(time - 10000))) - 0.5;
    vec2 finalCoord = texCoord + warpScale*(1 - edgeFactor*edgeFactor)*vec2(texOffsetX, texOffsetY);
    vec4 InTexel = texture(DiffuseSampler, finalCoord);

    float RedValue = dot(InTexel.rgb, vec3(a, b, c));
    float GreenValue = dot(InTexel.rgb, vec3(c, a, b));
    float BlueValue = dot(InTexel.rgb, vec3(b, c, a));

    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    float Luma = dot(OutColor, vec3(0.3, 0.59, 0.11));
    vec3 Chroma = OutColor - Luma;
    OutColor = Chroma * (1 + alpha*(0.8 + 0.1*IridLevel)) + Luma;

    fragColor = vec4(OutColor, 1.0);
}