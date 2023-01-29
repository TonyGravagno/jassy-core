package scriptcraft2;

public class Hook {

   public static enum HookType { CloseEnd, CloseStart, OpenEnd, OpenStart, Reload, Tick }

   public Runnable callback;
   public Boolean once;
   public HookType type;

   public Hook (HookType type, Runnable callback, Boolean once) {
      this.once = once;
      this.callback = callback;
      this.type = type;
      ScriptCraft2.hooks.add(this);
   }

   public void destroy () {
      if (this.callback != null) {
         ScriptCraft2.hooks.remove(this);
         this.callback = null;
      }
   }

   public void execute () {
      this.callback.run();
      if (this.once) {
         this.destroy();
      }
   }
}
