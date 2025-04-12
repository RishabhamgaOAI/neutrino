/*
package com.observeai.platform.realtime.neutrino.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.data.common.Moment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MomentsUtil {
     static String filePath = "/Users/in-prkalagara/Desktop/neutrino/src/main/resources/moments.json";
//    static String filePath = "/var/config/moments.json";
    static ObjectMapper objectMapper = new ObjectMapper();

    public static List<Moment> readFromFile() throws IOException {
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Moment>>() {
        });
    }

    public static List<String> getMissingMomentNames(List<Moment> moments,
                                                     Set<String> seenMomentIds) {
        List<String> missingMomentNames = new ArrayList<>();
        List<Moment> expectedMoments = moments.stream()
                .filter(Moment::isExpected)
                .collect(Collectors.toList());
        for (Moment moment : expectedMoments) {
            if (!seenMomentIds.contains(moment.getId())) {
                missingMomentNames.add(moment.getName());
            }
        }
        return missingMomentNames;
    }
}
*/
