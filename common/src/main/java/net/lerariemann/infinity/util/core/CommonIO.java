package net.lerariemann.infinity.util.core;

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
import java.util.function.BiFunction;

public interface CommonIO {
    static void write(NbtCompound base, String path, String filename) {
        write(compoundToString(base), Paths.get(path), filename);
    }

    static void write(NbtCompound base, Path dir, String filename) {
        write(compoundToString(base), dir, filename);
    }

    static void write(String source, Path dir, String filename) {
        List<String> lines = Collections.singletonList(source);
        filename = filename.replace("/", "_").replace("\\", "_");
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(filename);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void writeSurfaceRule(NbtCompound base, String path, String filename) {
        String source = compoundToString(base);
        for (int j: (new Integer[]{31, 62})) for (int i = -5; i < 6; i++) {
            String z = Integer.toString(j+i);
            source = source.replace("\"absolute\": "+z, "\"absolute\": "+(i==0 ? "%SL%" : "%SL" + (i>0 ? "+" : "") + i + "%"));
        }
        write(source, Paths.get(path), filename);
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

    static NbtCompound read(String path) {
        return read(new File(path));
    }
    static NbtCompound read(Path path) {
        return read(path.toFile());
    }
    static NbtCompound read(File file) {
        String content;
        try {
            if (file.exists()) {
                content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                NbtCompound c = StringNbtReader.parse(content);
                c.remove("infinity_version");
                return c;
            }
            return new NbtCompound();
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static NbtCompound readAndFormat(String path, Object... args) {
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
    static NbtCompound readAndAddCompound(String path, NbtCompound block) {
        return readAndFormat(path, compoundToString(block));
    }

    static NbtCompound readSurfaceRule(File file, int seaLevel) {
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            NbtCompound c = formatSurfaceRule(content, seaLevel);
            c.remove("infinity_version");
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static NbtCompound formatSurfaceRule(String content, int seaLevel) {
        int i = content.lastIndexOf("%");
        if (i == -1) {
            try {
                return StringNbtReader.parse(content);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        else if (content.contains("%SL%")) return formatSurfaceRule(content.replace("%SL%", String.valueOf(seaLevel)), seaLevel);
        else {
            int j = content.lastIndexOf("%SL");
            String num = content.substring(j+3, i);
            return formatSurfaceRule(content.replace("%SL" + num + "%", String.valueOf(seaLevel + Integer.parseInt(num))), seaLevel);
        }
    }

    static WeighedStructure<String> readStringList(String path) {
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
        if (!RandomProvider.rule("enforceModLoadedChecks")) return true;
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

    static <T> WeighedStructure<T> readWeighedList(Path path, String subPath, BiFunction<NbtCompound, String, T> infoGetter) {
        WeighedStructure<T> res = new WeighedStructure<>();
        for (File path1: Objects.requireNonNull(path.toFile().listFiles(File::isDirectory))) {
            NbtList list = _extractElements(path1, subPath);
            for(int i = 0; i < list.size(); i++) {
                NbtCompound a = list.getCompound(i);
                res.add(infoGetter.apply(a, "key"), a.getDouble("weight"));
            }
        }
        return res;
    }
    static WeighedStructure<String> readStringList(Path path, String subPath) {
        return readWeighedList(path, subPath, NbtCompound::getString);
    }
    static WeighedStructure<NbtElement> readCompoundList(Path path, String subPath) {
        return readWeighedList(path, subPath, NbtCompound::get);
    }

    static String appendTabs(String parent, int t) {
        return parent + "\t".repeat(Math.max(0, t));
    }

    static String elementToString(NbtElement base, int t) {
        if (base == null) {
            return "!!NULL!!";
        } else if (base instanceof NbtCompound nbtCompound) {
            return compoundToString(nbtCompound, t + 1);
        } else if (base instanceof NbtList nbtElements) {
            return listToString(nbtElements, t + 1);
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

    int maxBound = 30000000;
    static float boundsCheck(float base) {
        return MathHelper.clamp(base, -maxBound, maxBound);
    }
    static int boundsCheck(long base) {
        return (int) MathHelper.clamp(base, -maxBound, maxBound);
    }
    static int boundsCheck(int base) {
        return MathHelper.clamp(base, -maxBound, maxBound);
    }

    static String compoundToString(NbtCompound base) {
        return compoundToString(base, 0);
    }
    static String compoundToString(NbtCompound base, int t) {
        String res = "{\n";
        int i = base.getSize() - 1;
        for (String key: base.getKeys()) {
            NbtElement elem = base.get(key);
            res = appendTabs(res, t+1) + "\"" + key + "\": " + elementToString(elem, t);
            if (i!=0) res += ",";
            res += "\n";
            i--;
        }
        res = appendTabs(res, t) + "}";
        return res;
    }
    static String listToString(NbtList base, int t) {
        String res = "[\n";
        for (int i=0; i<base.size(); i++) {
            NbtElement elem = base.get(i);
            res = appendTabs(res, t+1) + elementToString(elem, t);
            if (i!=(base.size()-1)) res += ",";
            res += "\n";
        }
        res = appendTabs(res, t) + "]";
        return res;
    }
}
