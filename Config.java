import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static Properties properties = new Properties();

    static {
        try (InputStream input = new FileInputStream("config.ini")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}  