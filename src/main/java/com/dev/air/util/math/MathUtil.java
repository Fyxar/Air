package com.dev.air.util.math;

import com.dev.air.util.MinecraftInstance;
import net.minecraft.util.MathHelper;

import java.util.Random;

public class MathUtil {

    private static Random random = new Random(System.currentTimeMillis());

    public static double randomNormal(double min, double max) {
        return (max + (min - max) * Math.random());
    }

    public static double randomDeviated(double medium, double deviation, double maxDeviation) {
        return medium + deviation * MathHelper.clamp_double(random.nextGaussian(), -maxDeviation, maxDeviation);
    }

    public static double randomNoise(double min, double max) {
        FastNoiseLite perlinNoise = new FastNoiseLite((int) randomNormal(0, 10000));
        perlinNoise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);

        float perlinValue = perlinNoise.GetNoise((float) randomNormal(0, 10000),
                (float) randomNormal(0, 10000) + (float) Math.random());
        return map(perlinValue, -1.0f, 1.0f, (float) min, (float) max);
    }

    public static double randomLast(double min, double max, long last) {
        long timeDifference = (long) ((System.currentTimeMillis() - last) + randomNormal(1, 10));
        return (timeDifference % (max - min + 1)) + min;
    }

    private static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
    }

}
