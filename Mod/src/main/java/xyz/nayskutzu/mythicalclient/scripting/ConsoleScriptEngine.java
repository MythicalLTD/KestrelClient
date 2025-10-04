package xyz.nayskutzu.mythicalclient.scripting;

import net.minecraft.client.Minecraft;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.gui.ConsoleLogBuffer;
import xyz.nayskutzu.mythicalclient.utils.ChatColor;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class ConsoleScriptEngine {

    private static final ConsoleScriptEngine INSTANCE = new ConsoleScriptEngine();
    private final ScriptEngine engine;

    private ConsoleScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine eng = null;
        try {
            // Try common aliases first
            String[] names = new String[] {"nashorn", "JavaScript", "javascript", "js"};
            for (String n : names) {
                eng = manager.getEngineByName(n);
                if (eng != null) break;
            }
            // Reflective fallback for Nashorn factory
            if (eng == null) {
                try {
                    Class<?> f = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
                    Object factory = f.newInstance();
                    eng = (ScriptEngine) f.getMethod("getScriptEngine").invoke(factory);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        this.engine = eng;
    }

    public static ConsoleScriptEngine getInstance() {
        return INSTANCE;
    }

    public synchronized String evaluate(String code) {
        if (engine == null) {
            return "<no scripting engine available>";
        }
        try {
            Bindings bindings = new SimpleBindings();
            bindings.put("mc", Minecraft.getMinecraft());
            bindings.put("mod", MythicalClientMod.instance);
            bindings.put("chat", (ConsolePrinter) this::println);
            bindings.put("ChatColor", ChatColor.class);

            Object result = engine.eval(code, bindings);
            return String.valueOf(result);
        } catch (ScriptException e) {
            return "Error: " + e.getMessage();
        } catch (Throwable t) {
            return "Error: " + t.toString();
        }
    }

    private void println(String line) {
        ConsoleLogBuffer.getInstance().append(line);
    }

    @FunctionalInterface
    public interface ConsolePrinter {
        void print(String line);
    }
} 