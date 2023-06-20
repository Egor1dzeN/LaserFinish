import arduino.Arduino;

import javax.script.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

public class AppJava {
    public static void main(String[] args) throws ScriptException, IOException {

        StringWriter writer = new StringWriter(); //ouput will be stored here

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptContext context = new SimpleScriptContext();

        context.setWriter(writer); //configures output redirection
        ScriptEngine engine = manager.getEngineByName("python");
        engine.eval(new FileReader("C:\\Users\\egorm\\PycharmProjects\\pythonProject5\\man2.py"), context);
        System.out.println(writer.toString());
    }
}
