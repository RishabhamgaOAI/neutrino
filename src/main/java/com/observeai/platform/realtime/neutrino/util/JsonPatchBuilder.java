package com.observeai.platform.realtime.neutrino.util;

import java.util.ArrayList;
import java.util.List;

public class JsonPatchBuilder {

    private List<String> operations;
    private String path;

    public JsonPatchBuilder() {
        operations = new ArrayList<>();
        path = "";
    }

    public JsonPatchBuilder path(String value) {
        return path(value, false);
    }

    public JsonPatchBuilder path(String value, boolean reset) {
        path = (reset ? value : path.concat(value));
        return this;
    }

    public JsonPatchBuilder add(String path, String value) {
        String op = String.format("{ \"op\": \"%s\", \"path\": \"%s\", \"value\": \"%s\"}", "add", this.path.concat(path), value);
        operations.add(op);
        return this;
    }

    public JsonPatchBuilder replace(String path, String value) {
        String op = String.format("{ \"op\": \"%s\", \"path\": \"%s\", \"value\": \"%s\"}", "replace", this.path.concat(path), value);
        operations.add(op);
        return this;
    }

    public JsonPatchBuilder remove(String path) {
        String op = String.format("{ \"op\": \"%s\", \"path\": \"%s\"}", "replace", this.path.concat(path));
        operations.add(op);
        return this;
    }

    public JsonPatchBuilder copy(String from, String path) {
        String op = String.format("{ \"op\": \"%s\", \"path\": \"%s\", \"from\": \"%s\"}", "copy", this.path.concat(path), from);
        operations.add(op);
        return this;
    }

    public JsonPatchBuilder move(String from, String path) {
        String op = String.format("{ \"op\": \"%s\", \"path\": \"%s\", \"from\": \"%s\"}", "move", this.path.concat(path), from);
        operations.add(op);
        return this;
    }

    public String build() {
        String ops = String.join(",", operations);
        return String.format("[ %s ]", ops);
    }
}
