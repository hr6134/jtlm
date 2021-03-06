package com.glotitude.jtlm;

import java.util.List;

public interface TlmCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
