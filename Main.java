import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.text.DecimalFormat;

public class Main {

    public static void main(String[] args) throws IOException {
        Long go = System.nanoTime();
        Path root = Paths.get(args[0]);
        int klas = 0;
        List<Path> paths = smaliFiles(root);
        for (Path path: paths) {
            try {
                if (new String(Files.readAllBytes(path)).contains("const-string")) {
                    doString(path);
                    klas += 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        System.out.println(("Processed classes: " + Integer.toString(klas) + " / " + Integer.toString(paths.size())));
        DecimalFormat format = new DecimalFormat("0.00");
        System.out.println(("Duration: " + format.format((System.nanoTime() - go) / 1.0E9d) + " s"));
    }

    public static void doString(Path path) throws IOException {
        List<String> newLines =  new ArrayList<>();
        List<String> allLines = Files.readAllLines(path);
        for (String line : allLines) {
            if (line.startsWith("    const-string ")) {
                String[] str = line.split("(?=\"[^\"].*\")");
                if (str.length > 1) {
                    String tar = str[1].substring(1, str[1].length()-1);
                    //System.out.println(path.toString());
                    //System.out.println("line "+ Integer.toString(allLines.indexOf(line)+1));
                    //System.out.println(line);
                    line = line.replace(tar, decrypt(tar));
                    //System.out.println(line+"\n\n-----------------\n");
                }
            }
            newLines.add(line);
        }
        Files.write(path, newLines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        }

    public static String decrypt(String str){
        StringBuilder sb=new StringBuilder(str);
        sb.reverse();
        return sb.toString();
    }

    public static List<Path> smaliFiles(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Not a directory!");
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(path)) {
            paths = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".smali"))
                    .collect(Collectors.toList());
        }
        return paths;
    }
}
