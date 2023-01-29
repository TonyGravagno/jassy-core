package scriptcraft2;

import scriptcraft2.Hook.HookType;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.LinkedList;
import java.util.UUID;

import org.graalvm.polyglot.Value;

public class ScriptCraft2API {
   
   private final Instance instance;

   public ScriptCraft2API (Instance instance) {
      this.instance = instance;
   }

   public void destroy () {
      this.instance.destroy();
   }

   public void emit (String name, String... data) {
      if (ScriptCraft2.events.containsKey(name)) {
         new LinkedList<>(ScriptCraft2.events.get(name)).forEach(listener -> {
            try {
               listener.executeVoid((Object[]) data);
            } catch (Throwable error) {
               error.printStackTrace();
            }
         });
      }
   }

   public Instance fileInstance (String path) {
      return this.fileInstance(path, UUID.randomUUID().toString());
   }

   public Instance fileInstance (String path, String meta) {
      return new Instance(Instance.InstanceType.File, this.instance.root, path, meta, this.instance);
   }

   public String getMeta () {
      return this.instance.meta;
   }

   public String getRoot () {
      return this.instance.root;
   }

   public Hook hook (String name, Value script) {
      return this.hook(name, script, false);
   }

   public Hook hook (String name, Value script, Boolean once) {
      Hook hook = new Hook(HookType.valueOf(name), () -> {
         try {
            script.executeVoid();
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }, once);
      this.instance.hooks.add(hook);
      return hook;
   }

   public Task intervalTask (Value script, int duration, String... args) {
      Task task = new Task(Task.TaskType.Interval, script, duration, args);
      this.instance.tasks.add(task);
      return task;
   }

   public Class<?> load (File file, String name) throws ClassNotFoundException, MalformedURLException {
      URL link = file.toURI().toURL();
      String path = file.toPath().normalize().toString();
      return Class.forName(name, true, ScriptCraft2.loaders.computeIfAbsent(path, (key) -> new URLClassLoader(
         new URL[] { link },
         ScriptCraft2.class.getClassLoader()
      )));
   }

   public boolean off (String name, Value handler) {
      if (ScriptCraft2.events.containsKey(name)) {
         return ScriptCraft2.events.get(name).remove(handler); 
      } else {
         return false;
      }
   }
   
   public void on (String name, Value handler) {
      ScriptCraft2.events.computeIfAbsent(name, key -> new LinkedList<>()).add(handler);
   }

   public void reload () {
      ScriptCraft2.trigger(Hook.HookType.Reload);
      ScriptCraft2.events.clear();
      ScriptCraft2.loaders.clear();
      new Hook(Hook.HookType.Tick, () -> {
         ScriptCraft2.primary.close();
         try {
            ScriptCraft2.primary.open();
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }, true);
      this.swap();
   }

   public Instance scriptInstance (String code) {
      return this.scriptInstance(code, UUID.randomUUID().toString());
   }

   public Instance scriptInstance (String code, String meta) {
      return new Instance(Instance.InstanceType.Script, this.instance.root, code, meta, this.instance);
   }

   public void swap () {
      new Hook(Hook.HookType.Tick, () -> {
         this.instance.close();
         try {
            this.instance.open();
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }, true);
   }

   public Task timeoutTask (Value script, int duration, String... args) {
      Task task = new Task(Task.TaskType.Timeout, script, duration, args);
      this.instance.tasks.add(task);
      return task;
   }
}
