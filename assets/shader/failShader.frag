#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float progress;
void main() {
    vec4 textureColor = texture2D(u_texture, v_texCoords);
    float finalColor = (textureColor.r * 0.299 + textureColor.g * 0.114 + textureColor.b * 0.587) / 3.0;
    textureColor.r = mix(textureColor.r, finalColor, progress);
    textureColor.g = mix(textureColor.g, finalColor, progress);
    textureColor.b = mix(textureColor.b, finalColor, progress);
    gl_FragColor = textureColor;
}