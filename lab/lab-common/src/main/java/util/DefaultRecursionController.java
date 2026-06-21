package util;

import java.util.HashSet;
import java.util.Set;

public class DefaultRecursionController {
    private final Set<String> scriptStack = new HashSet<>();
    public void pushScript(String script) {
        scriptStack.add(script);
    }
    public void popScript(String script) {
        scriptStack.remove(script);
    }
    public boolean checkRecursion(String script) {
        return scriptStack.contains(script);
    }
}