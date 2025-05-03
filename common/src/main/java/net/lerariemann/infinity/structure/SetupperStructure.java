package net.lerariemann.infinity.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModStructureTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class SetupperStructure extends Structure {
    String id;
    public static final MapCodec<SetupperStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SetupperStructure.configCodecBuilder(instance),
            (Codec.STRING.fieldOf("id")).forGetter(a -> a.id)).apply(instance, SetupperStructure::new));

    SetupperStructure(Structure.Config config, String i) {
        super(config);
        id = i;
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        Structure shadow = context.dynamicRegistryManager().get(RegistryKeys.STRUCTURE).get(Identifier.of(id));
        return shadow == null ? Optional.empty() : shadow.getStructurePosition(context);
    }

    @Override
    public StructureType<?> getType() {
        return ModStructureTypes.SETUPPER.get();
    }
}
