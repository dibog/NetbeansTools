
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class TestManifest {
    public static void main(String[] args) throws IOException {
        final String KEY = "OpenIDE-Module-Specification-Version";

        Manifest m = new Manifest(new FileInputStream("manifest.mf"));
        Attributes attr = m.getMainAttributes();
        String spec = attr.getValue(KEY);
        attr.putValue(KEY, "1.2");
        m.write(System.out);
    }
}
