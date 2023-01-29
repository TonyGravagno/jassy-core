package scriptcraft2;

import org.graalvm.polyglot.Value;

public class Task {

   public static enum TaskType { Interval, Timeout }

   public String[] args;
   public int duration;
   public int lifetime = 0;
   public Value script;
   public TaskType type;
   
   public Task (TaskType type, Value script, int duration, String... args) {
      this.args = args;
      this.duration = duration;
      this.script = script;
      this.type = type;
      ScriptCraft2.tasks.add(this);
   }

   public void destroy () {
      if (this.script != null) {
         ScriptCraft2.tasks.remove(this);
         this.script = null;
      }
   }

   public void tick () {
      if (++this.lifetime == this.duration) {
         try {
            this.script.executeVoid((Object[]) this.args);
         } catch (Throwable error) {
            error.printStackTrace();
         }
         if (this.type == TaskType.Timeout) {
            this.destroy();
         } else if (this.type == TaskType.Interval) {
            this.lifetime = 0;
         }
      }
   }
}
