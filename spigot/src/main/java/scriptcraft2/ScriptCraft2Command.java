package scriptcraft2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import org.graalvm.polyglot.Value;

public class ScriptCraft2Command extends Command {

   public static final HashMap<String, HashMap<String, ScriptCraft2Command>> commandCache = new HashMap<>();
   public static CommandMap commandMap;

   public Value executor;
   public Value tabCompleter;

   public ScriptCraft2Command (String namespace, String name, String[] aliases) {
      super(name, "", "", Arrays.asList(aliases));
      ScriptCraft2Command.commandMap.register(namespace, this);
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      if (this.executor != null) {
         try {
            this.executor.executeVoid(sender, label, args);
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
      return true;
   }

   public void setExecutor (Value executor) {
      this.executor = executor;
   }

   public void setTabCompleter (Value tabCompleter) {
      this.tabCompleter = tabCompleter;
   }

   @Override
   public LinkedList<String> tabComplete (CommandSender sender, String alias, String[] args) {
      LinkedList<String> output = new LinkedList<>();
      if (this.tabCompleter != null) {
         try {
            Value input = this.tabCompleter.execute(sender, alias, args);
            for (long index = 0; index < input.getArraySize(); index++) {
               output.add(input.getArrayElement(index).toString());
            }
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
      return output;
   }
}
