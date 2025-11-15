package org.example;

import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CupsQueueParserTest {

    CupsQueueParser parser = new CupsQueueParser();

    @CsvSource({
            "example_0.html,0",
            "example_1.html,1"
    })
    @ParameterizedTest
    void parse(String input, int expected) throws IOException {
        int countJobsInQueue = parser.countJobsInQueue(loadFile(input));

        assertThat(countJobsInQueue).isEqualTo(expected);
    }

    String loadFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (var is = classLoader.getResourceAsStream(fileName)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}