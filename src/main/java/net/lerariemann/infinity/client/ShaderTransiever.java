package net.lerariemann.infinity.client;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShaderTransiever {
    static Object[] genMatrix(Random r) {
        List<Float> points = new ArrayList<>();
        float scale = 2 + r.nextFloat();
        points.add(0.0f);
        points.add(scale);
        for (int i = 0; i < 8; i++) points.add(scale * r.nextFloat());
        Collections.sort(points);
        Object[] res = new Object[9];
        for (int i = 0; i < 9; i++) {
            res[i] = points.get(i+1) - points.get(i);
        }
        return res;
    }

    public static PacketByteBuf buildPacket(ServerWorld destination) {
        PacketByteBuf buf = PacketByteBufs.create();
        String s = destination.getRegistryKey().getValue().toString();
        boolean bl = destination.getRegistryKey().getValue().toString().contains("generated_");
        buf.writeBoolean(bl);
        if(bl) {
            long id = Long.parseLong(s.substring(s.lastIndexOf("generated_") + 10));
            Random r = new Random(id);
            RandomProvider prov = ((MinecraftServerAccess)destination.getServer()).getDimensionProvider();
            if (prov.roll(r, "use_shaders")) {
                Object[] lst = genMatrix(r);
                NbtCompound c = CommonIO.readCarefully(prov.configPath + "util/shader.json", lst);
                buf.writeNbt(c);
            }
        }
        return buf;
    }

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        LogManager.getLogger().info("Packet received");
        boolean bl = buf.readBoolean();
        if (!bl) {
            client.execute(() -> ShaderLoader.reloadShaders(client, false));
            return;
        }
        NbtCompound c = buf.readNbt();
        client.execute(() -> {
            CommonIO.write(c, ShaderLoader.shaderDir(client), ShaderLoader.FILENAME);
            ShaderLoader.reloadShaders(client, true);
        });
    }

    public static void receive_simple(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        LogManager.getLogger().info("Packet received");
        client.execute(() -> ShaderLoader.reloadShaders(client, true));
    }
}
