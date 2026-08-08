package dev.redstone.packetlogger.logger.unpacker;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionUnpackerTest {

    static class Sample {
        String name = "test";
        int count = 42;
        List<String> tags = List.of("a", "b");
        Map<String, Integer> stats = Map.of("hp", 20);
        Nested nested = new Nested();
    }

    static class Nested {
        double value = 1.5;
        boolean flag = true;
    }

    @Test
    void logsAllFieldsRecursively() {
        String out = ReflectionUnpacker.unpackWithReflection(new Sample());
        System.out.println("==== REFLECTION OUTPUT: " + out);
        assertTrue(out.contains("name"));
        assertTrue(out.contains("count"));
        assertTrue(out.contains("tags"));
        assertTrue(out.contains("stats"));
        assertTrue(out.contains("nested"));
        assertTrue(out.contains("value"));
        assertTrue(out.contains("flag"));
    }
}
