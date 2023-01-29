package scriptcraft2;

import java.net.URLClassLoader;

import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;

import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;

public class ScriptCraft2 {
   
   public static Engine engine;
   public static final HashMap<String, LinkedList<Value>> events = new HashMap<>();
   public static final LinkedList<Hook> hooks = new LinkedList<>();
   public static final HashMap<String, URLClassLoader> loaders = new HashMap<>();
   public static Instance primary;
   public static LinkedList<Task> tasks = new LinkedList<>();

   public static void close () {
      ScriptCraft2.trigger(Hook.HookType.CloseStart);
      ScriptCraft2.primary.destroy();
      ScriptCraft2.primary = null;
      ScriptCraft2.trigger(Hook.HookType.CloseEnd);
   }

   public static void open (String root) {
      if (ScriptCraft2.engine == null) {
         ScriptCraft2.engine = Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build();
      }
      ScriptCraft2.trigger(Hook.HookType.OpenStart);

      Paths.get(root).toFile().mkdir();
      String source = "index.js";
      Config[] configs = {
         new Config(Config.ConfigType.JSON, root, ".scriptcraft2rc", false),
         new Config(Config.ConfigType.YAML, root, "config.yml", false),
         new Config(Config.ConfigType.JSON, root, "scriptcraft2.json", false),
         new Config(Config.ConfigType.JSON, root, "package.json", true)
      };
      for (Config config : configs) {
         String main = config.getMain();
         if (main != null) {
            source = main;
            break;
         }
      }
      try {
         ScriptCraft2.primary = new Instance(Instance.InstanceType.File, root, source, null, null);
         ScriptCraft2.primary.open();
      } catch (Throwable error) {
         error.printStackTrace();
      }
      ScriptCraft2.trigger(Hook.HookType.OpenEnd);
   }

   public static void tick () {
      ScriptCraft2.trigger(Hook.HookType.Tick);
      new LinkedList<>(ScriptCraft2.tasks).forEach(task -> task.tick());
   }

   public static void trigger (Hook.HookType type) {
      new LinkedList<>(ScriptCraft2.hooks).forEach(hook -> {
         if (hook.type == type) {
            hook.execute();
         }
      });
   }
}
