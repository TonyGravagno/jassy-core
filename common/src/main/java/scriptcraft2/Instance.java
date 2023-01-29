package scriptcraft2;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedList;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class Instance {

   public static enum InstanceType { File, Script }
   
   public final LinkedList<Instance> children = new LinkedList<>();
   public final LinkedList<Hook> hooks = new LinkedList<>();
   public final LinkedList<Task> tasks = new LinkedList<>();
   public String meta;
   public Instance parent;
   public String root;
   public Context context;
   public String source;
   public InstanceType type;

   public Instance (InstanceType type, String root, String source, String meta, Instance parent) {
      this.meta = meta;
      this.parent = parent;
      this.root = root;
      this.source = source;
      this.type = type;
      if (this.parent != null) {
         this.parent.children.add(this);
      }
   }

   public void close () {
      if (this.context != null) {
         new LinkedList<>(this.children).forEach(child -> child.destroy());
         new LinkedList<>(this.hooks).forEach(hook -> hook.destroy());
         new LinkedList<>(this.tasks).forEach(task -> task.destroy());
         this.context.close();
         this.context = null;
      }
   }

   public void destroy () {
      this.close();
      if (this.parent != null) {
         this.parent.children.remove(this);
      }
   }

   public void execute () throws IOException {
      if (this.type == InstanceType.File) {
         Path path = Paths.get(this.root, this.source);
         File file = path.toFile();
         if (file.exists()) {
            String code = Files.readString(path).toString();
            try {
               this.context.eval("js", code);
            } catch (Throwable error) {
               error.printStackTrace();
            }
         } else {
            file.createNewFile();
         }
      } else if (this.type == InstanceType.Script) {
         try {
            this.context.eval("js", this.source);
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
   }

   public void open () throws IOException {
      if (this.context == null) {
         this.context = Context.newBuilder("js")
            .engine(ScriptCraft2.engine)
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
            .option("js.commonjs-require", "true")
            .option("js.ecmascript-version", "2022")
            .option("js.commonjs-require-cwd", this.root)
            .build();
         this.context.getBindings("js").putMember("ScriptCraft2", Value.asValue(new ScriptCraft2API(this)));
         try {
            this.execute();
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
   }
}
