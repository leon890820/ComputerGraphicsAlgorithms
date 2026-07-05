package org.example.engine.gl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL33.*;

public class Shader {

    private int programId;

    // ===== Constructor =====

    // fragment only（你原本有這個接口）
    public Shader(String fragPath) {
        this("/shaders/default.vert", fragPath);
    }

    public Shader(String vertPath, String fragPath) {
        String vertSrc = loadResource(vertPath);
        String fragSrc = loadResource(fragPath);

        int vertShader = compile(GL_VERTEX_SHADER, vertSrc);
        int fragShader = compile(GL_FRAGMENT_SHADER, fragSrc);

        programId = glCreateProgram();
        glAttachShader(programId, vertShader);
        glAttachShader(programId, fragShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader link failed:\n" + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertShader);
        glDeleteShader(fragShader);
    }

    // ===== API for Material =====

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getProgramId() {
        return programId;
    }

    public void delete() {
        glDeleteProgram(programId);
    }

    // ===== internal =====

    private int compile(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile failed:\n" + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    private String loadResource(String path) {
        InputStream is = Shader.class.getResourceAsStream(path);

        if (is == null) {
            throw new RuntimeException("Shader not found: " + path);
        }

        return new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}