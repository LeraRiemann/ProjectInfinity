#version 150
precision mediump float;
uniform float Time; //0 to 1
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
    vec4 InTexel = texture(DiffuseSampler, texCoord);
    float time = 2*Time*3.1415f;
    float iTime = 10*time + 1.5*noise(vec3(1.5*screenCoords, 3*(time > 0.5 ? 1 - time : time)));
    float a = (1.0f - cos(iTime))/3.0f + cos(iTime);
    float b = (1.0f - cos(iTime))/3.0f - sin(iTime) / sqrt(3);
    float c = (1.0f - cos(iTime))/3.0f + sin(iTime) / sqrt(3);

    float RedValue = dot(InTexel.rgb, vec3(a, b, c));
    float GreenValue = dot(InTexel.rgb, vec3(c, a, b));
    float BlueValue = dot(InTexel.rgb, vec3(b, c, a));

    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    float Luma = dot(OutColor, vec3(0.3, 0.59, 0.11));
    vec3 Chroma = OutColor - Luma;
    OutColor = (Chroma * 1.8) + Luma;

    fragColor = vec4(OutColor, 1.0);
}