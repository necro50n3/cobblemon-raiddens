package com.necro.raid.dens.common.util;

import java.util.List;
import java.util.Map;

public interface IProperties {
    Map<String, List<String>> crd_getMovesetBuilder();
    void crd_setMovesetBuilder(Map<String, List<String>> builder);
}
