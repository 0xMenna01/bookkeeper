package org.apache.bookkeeper.tls.utils;

import org.apache.bookkeeper.tls.utils.enums.ConfigType;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TestUtils {


    public static Collection<Object[]> buildAuthConfigParameters() {
        List<Object[]> parameters = new ArrayList<>();

        for (ConfigType configType : ConfigType.values()) {
            for (GenericInstance connectionPeerInstance : GenericInstance.values()) {
                for (GenericInstance authCallbackInstance : GenericInstance.values()) {
                    if (authCallbackInstance.equals(GenericInstance.INVALID))
                        continue;
                    boolean shouldThrowException = shouldThrowException(configType);
                    Object[] parameterSet = { configType, connectionPeerInstance, authCallbackInstance, shouldThrowException };
                    parameters.add(parameterSet);
                }
            }
        }

        return parameters;
    }

    private static boolean shouldThrowException(ConfigType configType) {
        List<ConfigType> exceptionConfig = List.of(new ConfigType[]{ConfigType.INVALID, ConfigType.NULL, ConfigType.EMPTY});
        return exceptionConfig.contains(configType);
    }

    public static List<GenericInstance> buildDefaultInstances() {
        return List.of(GenericInstance.values());
    }

    public static String[] buildCertRole(ConfigType authConfig) {
        if (authConfig.equals(ConfigType.NULL)) {
            return null;
        }

        String[] certRoles;
        if (authConfig.getRoles().contains(",")) {
            certRoles = authConfig.getRoles().split(",");
        } else {
            certRoles = new String[]{authConfig.getRoles()};
        }

        return certRoles;
    }


}
