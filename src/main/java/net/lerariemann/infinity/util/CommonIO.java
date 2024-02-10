package net.lerariemann.infinity.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

    public static void write(NbtCompound base, String path, String filename) {
        String source = CompoundToString(base, 0);
        List<String> lines = Collections.singletonList(source);
        Path dir = Paths.get(path);
        Path file = Paths.get(path+"/"+filename);
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

    public static NbtCompound read(File file) {
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return StringNbtReader.parse(content);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readCarefully(String path,  Object... args) {
        File file = new File(path);
        try {
            String content = String.valueOf((new Formatter(Locale.US)).format(FileUtils.readFileToString(file, StandardCharsets.UTF_8), args));
            return StringNbtReader.parse(content);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readAndAddBlock(String path, NbtCompound block) {
        File file = new File(path);
        try {
            String content = String.valueOf((new Formatter(Locale.US)).format(FileUtils.readFileToString(file, StandardCharsets.UTF_8), CompoundToString(block, 0)));
            return StringNbtReader.parse(content);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readSurfaceRule(File file, int sealevel) {
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return format(content, sealevel);
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

    public static WeighedStructure<String> weighedListReader(String path, String subpath) {
        WeighedStructure<String> res = new WeighedStructure<>();
        for (File path1: Objects.requireNonNull((new File(path)).listFiles(File::isDirectory))) {
            File readingthis = path1.toPath().resolve(subpath).toFile();
            if (readingthis.exists()) {
                NbtCompound base = read(readingthis);
                NbtList list = base.getList("elements", NbtElement.COMPOUND_TYPE);
                for(int i = 0; i < list.size(); i++) {
                    NbtCompound a = list.getCompound(i);
                    res.add(a.getString("key"), a.getDouble("weight"));
                }
            }
        }
        return res;
    }

    public static WeighedStructure<NbtElement> blockListReader(String path, String subpath) {
        WeighedStructure<NbtElement> res = new WeighedStructure<>();
        for (File path1: Objects.requireNonNull((new File(path)).listFiles(File::isDirectory))) {
            File file = path1.toPath().resolve(subpath).toFile();
            if (file.exists()) {
                NbtCompound base = read(file);
                NbtList list = base.getList("elements", NbtElement.COMPOUND_TYPE);
                for(int i = 0; i < list.size(); i++) {
                    NbtCompound a = list.getCompound(i);
                    res.add(a.get("key"), a.getDouble("weight"));
                }
            }
        }
        return res;
    }

    public static NbtList nbtListReader(String path, String subpath) {
        NbtList res = new NbtList();
        for (File path1: Objects.requireNonNull((new File(path)).listFiles(File::isDirectory))) {
            File readingthis = new File(path1.getPath() + "/" + subpath);
            if (readingthis.exists()) {
                NbtList add = read(path1.getPath() + "/" + subpath).getList("elements", NbtElement.STRING_TYPE);
                res.addAll(add);
            }
        }
        return res;
    }

    public static String appendTabs(String parent, int t) {
        return parent + "\t".repeat(Math.max(0, t));
    }

    public static String ElementToString(NbtElement base, int t) {
        String str;
        if (base == null) str = "!!NULL!!";
        else if (base instanceof NbtCompound) str = CompoundToString((NbtCompound)base, t+1);
        else if (base instanceof NbtList) str = ListToString((NbtList)base, t+1);
        else if (base instanceof NbtByte) str = (((NbtByte)base).byteValue() != 0) ? "true" : "false";
        else if (base instanceof NbtDouble) str = String.valueOf(((NbtDouble) base).floatValue());
        else if (base instanceof NbtFloat) str = String.valueOf(((NbtFloat) base).floatValue());
        else str = base.toString();
        return str;
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

