package org.apache.bookkeeper.tls.utils;

import org.apache.bookkeeper.tls.utils.enums.ConfigType;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TestUtils {


    public static Collection<Object[]> buildAuthConfigParameters() {
        return Arrays.asList(new Object[][] {
            //CONFIG               EXCEPTION
            { ConfigType.VALID_SINGLE_ROLE,     false},
            { ConfigType.VALID_MULTIPLE_ROLES,     false},
            { ConfigType.EMPTY,     true},
            { ConfigType.INVALID,     true},
            { ConfigType.NULL,     true},
        });
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
