package net.lerariemann.infinity.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.platform.Platform;
import net.minecraft.nbt.*;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public interface CommonIO {
    static void write(NbtCompound base, String path, String filename) {
        write(CompoundToString(base, 0), Paths.get(path), filename);
    }

    static void write(NbtCompound base, Path dir, String filename) {
        write(CompoundToString(base, 0), dir, filename);
    }

    static void write(String source, Path dir, String filename) {
        List<String> lines = Collections.singletonList(source);
        filename = filename.replace("/", "_").replace("\\", "_");
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(finalFile);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void writeSurfaceRule(NbtCompound base, String path, String filename) {
        String source = CompoundToString(base, 0);
        for (int j: (new Integer[]{31, 62})) for (int i = -5; i < 6; i++) {
            String z = Integer.toString(j+i);
            source = source.replace("\"absolute\": "+z, "\"absolute\": "+(i==0 ? "%SL%" : "%SL" + (i>0 ? "+" : "") + i + "%"));
        }
        write(source, Paths.get(path), filename);
    }

    static NbtCompound read(String path) {
        return read(new File(path));
    }

    static int getVersion(File file) {
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (!content.contains("infinity_version")) return 0;
            int i = content.indexOf("infinity_version");
            int end = content.indexOf(",", i);
            if (end == -1) {
                end = content.indexOf("\n", i);
            }
            return Integer.parseInt(content.substring(content.indexOf(" ", i)+1, end).trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static NbtCompound read(File file) {
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

    static NbtCompound readCarefully(String path,  Object... args) {
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

    static NbtCompound readAndAddBlock(String path, NbtCompound block) {
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

    static NbtCompound readSurfaceRule(File file, int sealevel) {
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            NbtCompound c = format(content, sealevel);
            c.remove("infinity_version");
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static NbtCompound format(String content, int sealevel) {
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

    static WeighedStructure<String> stringListReader(String path) {
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
        return Platform.isModLoaded(modname);
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

    static WeighedStructure<String> stringListReader(String path, String subpath) {
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

    static WeighedStructure<NbtElement> compoundListReader(String path, String subpath) {
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

    static String appendTabs(String parent, int t) {
        return parent + "\t".repeat(Math.max(0, t));
    }

    static String ElementToString(NbtElement base, int t) {
        if (base == null) {
            return "!!NULL!!";
        } else if (base instanceof NbtCompound nbtCompound) {
            return CompoundToString(nbtCompound, t + 1);
        } else if (base instanceof NbtList nbtElements) {
            return ListToString(nbtElements, t + 1);
        } else if (base instanceof NbtByte nbtByte) {
            return (nbtByte.byteValue() != 0) ? "true" : "false";
        } else if (base instanceof NbtDouble nbtDouble) {
            return String.valueOf(boundsCheck(nbtDouble.floatValue()));
        } else if (base instanceof NbtFloat nbtFloat) {
            return String.valueOf(boundsCheck(nbtFloat.floatValue()));
        } else if (base instanceof NbtLong nbtLong) {
            return String.valueOf(boundsCheck(nbtLong.longValue()));
        } else if (base instanceof NbtInt nbtInt) {
            return String.valueOf(boundsCheck(nbtInt.intValue()));
        } else if (base instanceof NbtShort nbtShort) {
            return String.valueOf(boundsCheck(nbtShort.shortValue()));
        } else if (base instanceof NbtString nbtString) {
            return "\"" + nbtString.asString().replace("\"", "\\\"") + "\"";
        }
        return base.toString();
    }

    static float boundsCheck(float base) {
        return MathHelper.clamp(base, -2048, 2048);
    }
    static float boundsCheck(long base) {
        return MathHelper.clamp(base, -2048, 2048);
    }
    static int boundsCheck(int base) {
        return MathHelper.clamp(base, -2048, 2048);
    }

    static String CompoundToString(NbtCompound base, int t) {
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
    static String ListToString(NbtList base, int t) {
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

