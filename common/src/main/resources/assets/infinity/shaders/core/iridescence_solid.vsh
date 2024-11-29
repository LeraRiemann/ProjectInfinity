#version 150

in vec3 Position;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;
uniform float GameTime;

out float vertexDistance;
out vec4 vertexColor;

float fog_distance(vec3 pos, int shape) {
    if (shape == 0) {
        return length(pos);
    } else {
        float distXZ = length(pos.xz);
        float distY = abs(pos.y);
        return max(distXZ, distY);
    }
}
vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

vec4 hue_rotate(vec4 color, float hue) {
    float iTime = 2*hue*3.1415f;
    float a = (1.0f - cos(iTime))/3.0f + cos(iTime);
    float b = (1.0f - cos(iTime))/3.0f - sin(iTime) / sqrt(3);
    float c = (1.0f - cos(iTime))/3.0f + sin(iTime) / sqrt(3);
    float RedValue = dot(color.rgb, vec3(a, b, c));
    float GreenValue = dot(color.rgb, vec3(c, a, b));
    float BlueValue = dot(color.rgb, vec3(b, c, a));
    return vec4(RedValue, GreenValue, BlueValue, color.a);
}

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(pos, FogShape);
    float hue = (Position.x + Position.y + Position.z) / 12.0 + 12*GameTime;
    vertexColor = hue_rotate(vec4(1, 0, 0, 1), hue);
}