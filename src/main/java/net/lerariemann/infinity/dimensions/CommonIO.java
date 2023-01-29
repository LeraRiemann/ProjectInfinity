package net.lerariemann.infinity.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

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
    public static NbtCompound read(String path) throws IOException, CommandSyntaxException {
        File file = new File(path);
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return StringNbtReader.parse(content);
    }

    public static NbtCompound readCarefully(String path, int i) throws IOException, CommandSyntaxException {
        File file = new File(path);
        String content = String.format(FileUtils.readFileToString(file, StandardCharsets.UTF_8), i);
        return StringNbtReader.parse(content);
    }

    public static WeighedStructure<String> commonListReader(String path) throws IOException, CommandSyntaxException {
        NbtCompound base = read(path);
        WeighedStructure<String> res = new WeighedStructure<>();
        NbtList list = base.getList("elements", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < list.size(); i++) {
            NbtCompound a = list.getCompound(i);
            res.add(a.getString("key"), a.getDouble("weight"));
        }
        return res;
    }

    public static String appendTabs(String parent, int t) {
        return parent + "\t".repeat(Math.max(0, t));
    }

    public static String ElementToString(NbtElement base, int t) {
        String str;
        if (base instanceof NbtCompound) str = CompoundToString((NbtCompound)base, t+1);
        else if (base instanceof NbtList) str = ListToString((NbtList)base, t+1);
        else if (base instanceof NbtByte) str = (((NbtByte)base).byteValue() != 0) ? "true" : "false";
        else if (base instanceof NbtDouble) str = String.valueOf(((NbtDouble) base).floatValue());
        else if (base instanceof NbtFloat) str = String.valueOf(((NbtFloat) base).floatValue());
        else str = base.toString();
        return  str;
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

