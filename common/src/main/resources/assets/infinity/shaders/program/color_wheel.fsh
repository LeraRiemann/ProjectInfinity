#version 150
precision mediump float;
uniform float Time; //0 to 1
uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 InTexel = texture(DiffuseSampler, texCoord);

    float iTime = Time * 2 * 3.1415f;
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