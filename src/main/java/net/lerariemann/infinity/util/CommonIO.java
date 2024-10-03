package net.lerariemann.infinity.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CommonIO {

    public static void write(NbtCompound base, Path dir, String filename) {
        String source = CompoundToString(base, 0);
        List<String> lines = Collections.singletonList(source);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(filename);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(NbtCompound base, String path, String filename) {
        write(CompoundToString(base, 0), path, filename);
    }

    public static void writeSurfaceRule(NbtCompound base, String path, String filename) {
        String source = CompoundToString(base, 0);
        source = source.replace(": Infinity", ": 2147483647");
        for (int j: (new Integer[]{31, 62})) for (int i = -5; i < 6; i++) {
            String z = Integer.toString(j+i);
            source = source.replace("\"absolute\": "+z, "\"absolute\": "+(i==0 ? "%SL%" : "%SL" + (i>0 ? "+" : "") + i + "%"));
        }
        write(source, path, filename);
    }

    public static void write(String source, String path, String filename) {
        List<String> lines = Collections.singletonList(source);
        Path file = Paths.get(path + File.separator + filename);
        Path dir = file.getParent();
        try {
            Files.createDirectories(dir);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound read(String path) {
        return read(new File(path));
    }

    public static int getVersion(File file) {
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (!content.contains("infinity_version")) return 0;
            int i = content.indexOf("infinity_version");
            return Integer.parseInt(content.substring(content.indexOf(" ", i)+1, content.indexOf(",", i)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound read(File file) {
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            NbtCompound c = StringNbtReader.parse(content);
            c.remove("infinity_version");
            return c;
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readCarefully(String path,  Object... args) {
        File file = new File(path);
        try {
            String content = String.valueOf((new Formatter(Locale.US)).format(FileUtils.readFileToString(file, StandardCharsets.UTF_8), args));
            NbtCompound c = StringNbtReader.parse(content);
            c.remove("infinity_version");
            return c;
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readAndAddBlock(String path, NbtCompound block) {
        File file = new File(path);
        try {
            String content = String.valueOf((new Formatter(Locale.US)).format(FileUtils.readFileToString(file, StandardCharsets.UTF_8), CompoundToString(block, 0)));
            NbtCompound c = StringNbtReader.parse(content);
            c.remove("infinity_version");
            return c;
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readSurfaceRule(File file, int sealevel) {
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            NbtCompound c = format(content, sealevel);
            c.remove("infinity_version");
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound format(String content, int sealevel) {
        int i = content.lastIndexOf("%");
        if (i == -1) {
            try {
                return StringNbtReader.parse(content);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        else if (content.contains("%SL%")) return format(content.replace("%SL%", String.valueOf(sealevel)), sealevel);
        else {
            int j = content.lastIndexOf("%SL");
            String num = content.substring(j+3, i);
            return format(content.replace("%SL" + num + "%", String.valueOf(sealevel + Integer.parseInt(num))), sealevel);
        }
    }

    public static WeighedStructure<String> weighedListReader(String path) {
        NbtCompound base = read(path);
        WeighedStructure<String> res = new WeighedStructure<>();
        NbtList list = base.getList("elements", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < list.size(); i++) {
            NbtCompound a = list.getCompound(i);
            res.add(a.getString("key"), a.getDouble("weight"));
        }
        return res;
    }

    private static boolean _checkIfModLoaded(File path1) {
        String modname = path1.toPath().getName(path1.toPath().getNameCount() - 1).toString();
        return FabricLoader.getInstance().isModLoaded(modname);
    }

    private static NbtList _extractElements(File path1, String subpath) {
        if (_checkIfModLoaded(path1)) {
            File file = path1.toPath().resolve(subpath).toFile();
            if (file.exists()) {
                NbtCompound base = read(file);
                return base.getList("elements", NbtElement.COMPOUND_TYPE);
            }
        }
        return new NbtList();
    }

    public static WeighedStructure<String> weighedListReader(String path, String subpath) {
        WeighedStructure<String> res = new WeighedStructure<>();
        for (File path1: Objects.requireNonNull((new File(path)).listFiles(File::isDirectory))) {
            NbtList list = _extractElements(path1, subpath);
            for(int i = 0; i < list.size(); i++) {
                NbtCompound a = list.getCompound(i);
                res.add(a.getString("key"), a.getDouble("weight"));
            }
        }
        return res;
    }

    public static WeighedStructure<NbtElement> blockListReader(String path, String subpath) {
        WeighedStructure<NbtElement> res = new WeighedStructure<>();
        for (File path1: Objects.requireNonNull((new File(path)).listFiles(File::isDirectory))) {
            NbtList list = _extractElements(path1, subpath);
            for(int i = 0; i < list.size(); i++) {
                NbtCompound a = list.getCompound(i);
                res.add(a.get("key"), a.getDouble("weight"));
            }
        }
        return res;
    }

    public static NbtList nbtListReader(String path, String subpath) {
        NbtList res = new NbtList();
        for (File path1: Objects.requireNonNull((new File(path)).listFiles(File::isDirectory))) {
            if (_checkIfModLoaded(path1)) {
                File readingthis = new File(path1.getPath() + "/" + subpath);
                if (readingthis.exists()) {
                    NbtList add = read(path1.getPath() + "/" + subpath).getList("elements", NbtElement.STRING_TYPE);
                    res.addAll(add);
                }
            }
        }
        return res;
    }

    public static String appendTabs(String parent, int t) {
        return parent + "\t".repeat(Math.max(0, t));
    }

    public static String ElementToString(NbtElement base, int t) {
        return switch (base) {
            case null -> "!!NULL!!";
            case NbtCompound nbtCompound -> CompoundToString(nbtCompound, t + 1);
            case NbtList nbtElements -> ListToString(nbtElements, t + 1);
            case NbtByte nbtByte -> (nbtByte.byteValue() != 0) ? "true" : "false";
            case NbtDouble nbtDouble -> String.valueOf(nbtDouble.floatValue());
            case NbtFloat nbtFloat -> String.valueOf(nbtFloat.floatValue());
            default -> base.toString();
        };
    }

    public static String CompoundToString(NbtCompound base, int t) {
        String res = "{\n";
        int i = base.getSize() - 1;
        for (String key: base.getKeys()) {
            NbtElement elem = base.get(key);
            res = appendTabs(res, t+1) + "\"" + key + "\": " + ElementToString(elem, t);
            if (i!=0) res += ",";
            res += "\n";
            i--;
        }
        res = appendTabs(res, t) + "}";
        return res;
    }
    public static String ListToString(NbtList base, int t) {
        String res = "[\n";
        for (int i=0; i<base.size(); i++) {
            NbtElement elem = base.get(i);
            res = appendTabs(res, t+1) + ElementToString(elem, t);
            if (i!=(base.size()-1)) res += ",";
            res += "\n";
        }
        res = appendTabs(res, t) + "]";
        return res;
    }
}

