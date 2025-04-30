package net.lerariemann.infinity.dimensions.features.surface_structures;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

public class RandomEndSpikes extends RandomisedFeature {
    public RandomEndSpikes(RandomFeaturesList parent) {
        super(parent, "spikes");
        id = "end_spike";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        if (random.nextBoolean()) config.putBoolean("crystal_invulnerable", true);
        if (random.nextBoolean()) {
            NbtList crystal_beam_target = new NbtList();
            crystal_beam_target.add(NbtInt.of(random.nextInt(-128, 128)));
            crystal_beam_target.add(NbtInt.of(daddy.min_y + random.nextInt(daddy.height)));
            crystal_beam_target.add(NbtInt.of(random.nextInt(-128, 128)));
            config.put("crystal_beam_target", crystal_beam_target);
        }
        NbtList spikes = new NbtList();
        if (PROVIDER.roll(random, "scatter_end_spikes")) {
            int i = random.nextInt(6, 24);
            for (int j = 0; j < i; j++) {
                NbtCompound spike = new NbtCompound();
                spike.putInt("centerX", random.nextInt(-128, 128));
                spike.putInt("centerZ", random.nextInt(-128, 128));
                spike.putInt("radius", random.nextInt(1, 8));
                int max = daddy.min_y + daddy.height;
                int min = Math.max(max - daddy.height / 2, daddy.sea_level);
                int mean = random.nextInt(min, max);
                int height = (int)Math.floor(random.nextGaussian(mean, 16));
                height = Math.max(height, min);
                height = Math.min(height, max);
                spike.putInt("height", height);
                if (PROVIDER.roll(random, "cage_crystals")) spike.putBoolean("guarded", true);
                spikes.add(spike);
            }
        }
        config.put("spikes", spikes);
        return feature(config);
    }
}
