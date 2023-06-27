package org.apache.bookkeeper.tls.utils;

import org.apache.bookkeeper.utils.GenericInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TestUtils {


    /// Used to build certificate roles
    private static final String ROLE_NAME = "CN=Test Name";
    private static final String ORGANIZATION_NAME = " O=Test Organization";
    private static final String ROLE_SEPARATOR = ",";

    public static class ExceptionExpected {
        private boolean isExceptionConfig;
        private boolean isExceptionProvider;

        public ExceptionExpected(boolean isExceptionConfig, boolean isExceptionProvider) {
            this.isExceptionConfig = isExceptionConfig;
            this.isExceptionProvider = isExceptionProvider;
        }

        public boolean configException() {
            return isExceptionConfig;
        }

        public boolean providerException() {
            return isExceptionProvider;
        }

        public boolean shouldThrow() {
            return (isExceptionConfig || isExceptionProvider);
        }
    }


    public static Collection<Object[]> buildAuthConfigParameters() {
        List<Object[]> parameters = new ArrayList<>();

        for (ConfigType configType : ConfigType.values()) {
            for (GenericInstance connectionPeerInstance : GenericInstance.values()) {
                for (GenericInstance authCallbackInstance : GenericInstance.values()) {
                    ExceptionExpected shouldThrowException = shouldThrowException(configType, connectionPeerInstance, authCallbackInstance);
                    Object[] parameterSet = {configType, connectionPeerInstance, authCallbackInstance, shouldThrowException};
                    parameters.add(parameterSet);
                }
            }
        }

        return parameters;
    }

    private static ExceptionExpected shouldThrowException(ConfigType configType, GenericInstance bookieConnectionInstance, GenericInstance authCallbackInstance) {
        List<ConfigType> exceptionConfig = Arrays.asList(ConfigType.NULL, ConfigType.INVALID);
        boolean isConfigException = exceptionConfig.contains(configType);
        boolean isProviderException = (bookieConnectionInstance.equals(GenericInstance.NULL) ||
            authCallbackInstance.equals(GenericInstance.NULL));

        return new ExceptionExpected(isConfigException, isProviderException);
    }


    public static String[] getRoles(ConfigType authConfig) {
        if (authConfig.equals(ConfigType.NULL)) {
            return null;
        }

        String[] certRoles;
        if (authConfig.getRoles().contains(ROLE_SEPARATOR)) {
            certRoles = authConfig.getRoles().split(ROLE_SEPARATOR);
        } else {
            certRoles = new String[]{authConfig.getRoles()};
        }

        return certRoles;
    }

    public static String buildCertRole(String[] roles) {

        if (roles == null) {
            return null;
        }

        if (roles.length == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(ROLE_NAME);
            sb.append(ROLE_SEPARATOR);
            sb.append(" OU=0:");
            sb.append(roles[0]);

            sb.append(ROLE_SEPARATOR);
            sb.append(ORGANIZATION_NAME);

            return sb.toString();
        }

        throw new IllegalStateException("This method only supports 1 role");

    }

}
