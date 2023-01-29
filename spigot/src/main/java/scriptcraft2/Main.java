package scriptcraft2;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.DriverManager;

import java.util.HashMap;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   @Override
   public void onLoad() {
      DriverManager.getDrivers();
      Loader loader = new Loader(this.getClassLoader());
      try {
         Class<?> clazz = ScriptCraft2.class;
         try {
            URL resource = clazz.getProtectionDomain().getCodeSource().getLocation();
            if (resource instanceof URL) {
               loader.addURL(resource);
            }
         } catch (Throwable error) {
            // do nothing
         }
         URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
         if (resource instanceof URL) {
            String link = resource.toString();
            String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
            if (link.endsWith(suffix)) {
               String path = link.substring(0, link.length() - suffix.length());
               if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
               try {
                  loader.addURL(new URL(path));
               } catch (Throwable error) {
                  // do nothing
               }
            }
         }
         Thread.currentThread().setContextClassLoader((ClassLoader) loader);
      } catch (Throwable error) {
         error.printStackTrace();
      }
      try {
         Field internal = this.getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         ScriptCraft2Command.commandMap = (CommandMap) internal.get(this.getServer());
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   @Override
   public void onEnable() {
      try {
         this.getServer().getScheduler().runTaskTimer(this, ScriptCraft2::tick, 0, 1);
      } catch (Throwable error) {
         // none
      }
      ScriptCraft2.open(this.getDataFolder().getPath());
   }

   @Override
   public void onDisable() {
      ScriptCraft2.close();
      ScriptCraft2Command.commandCache.values().forEach(subcache -> {
         subcache.values().forEach(command -> {
            command.setExecutor(null);
            command.setTabCompleter(null);
         });
      });
   }

   public void registerCommand (String name) {
      this.registerCommand(name, null, null, null, null, null);
   }

   public void registerCommand (String name, Value executor) {
      this.registerCommand(name, executor, null, null, null, null);
   }

   public void registerCommand (String name, Value executor, Value tabCompleter) {
      this.registerCommand(name, executor, tabCompleter, null, null, null);
   }

   public void registerCommand (
      String name,
      Value executor,
      Value tabCompleter,
      String permission
   ) {
      this.registerCommand(name, executor, tabCompleter, permission, null, null);
   }

   public void registerCommand (
      String name,
      Value executor,
      Value tabCompleter,
      String permission,
      String[] aliases
   ) {
      this.registerCommand(name, executor, tabCompleter, permission, aliases, null);
   }

   public void registerCommand (
      String name,
      Value executor,
      Value tabCompleter,
      String permission,
      String[] aliases,
      String namespace
   ) {
      ScriptCraft2Command command = ScriptCraft2Command.commandCache.computeIfAbsent(namespace, (key) -> {
         return new HashMap<>();
      }).computeIfAbsent(name, (key) -> {
         return new ScriptCraft2Command(namespace == null ? this.getName() : namespace, name, aliases == null ? new String[0] : aliases);
      });
      command.setExecutor(executor);
      command.setTabCompleter(tabCompleter);
      command.setPermission(permission);
   }
}
