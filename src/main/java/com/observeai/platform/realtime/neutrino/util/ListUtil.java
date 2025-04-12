package com.observeai.platform.realtime.neutrino.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    public static <K> List<K> emptyIfNull(List<K> list) {
        if (list == null)
            return new ArrayList<>();
        return list;
    }
}
