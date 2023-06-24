package org.apache.bookkeeper.client.utils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClientUtils {

    public static Collection<Object[]> buildParameters() {
        List<Object[]> parameters = new ArrayList<>();

        int i = 0;
        int[] ackQuorumSizeParams = {-1, 0, 1};
        for (int ackQuorumSize : ackQuorumSizeParams) {
            int[] writeQuorumSizeParams = {ackQuorumSize - 1, ackQuorumSize, ackQuorumSize + 1};
            for (int writeQuorumSize : writeQuorumSizeParams) {
                int[] ensembleSizeParams = {writeQuorumSize - 1, writeQuorumSize, writeQuorumSize + 1};
                for (int ensembleSize : ensembleSizeParams) {
                    MapType mapType = buildCustomMetaParam(i);

                    ExceptionExpected exceptionExpected = buildMetaException(ackQuorumSize, writeQuorumSize, ensembleSize, mapType);

                    Object[] parameterSet = {ensembleSize, writeQuorumSize, ackQuorumSize, mapType, exceptionExpected};
                    parameters.add(parameterSet);

                    i++;
                }
            }
        }

        return parameters;
    }

    private static MapType buildCustomMetaParam(int numTest) {
        MapType mapType;
        if (numTest <= 6) {
            mapType = MapType.NULL;
        } else if (numTest > 6 && numTest <= 12) {
            mapType = MapType.INVALID;
        } else if (numTest > 12 && numTest <= 19) {
            mapType = MapType.VALID;
        } else if (numTest > 19 && numTest <= 26) {
            mapType = MapType.EMPTY;
        } else {
            throw new RuntimeException("Number of arguments does not match the required");
        }

        return mapType;
    }

    private static ExceptionExpected buildMetaException(int ackQuorumSize, int writeQuorumSize, int ensembleSize, MapType mapType) {
        ExceptionExpected exception = new ExceptionExpected();

        if (ackQuorumSize <= 0 || writeQuorumSize <= 0 || ensembleSize <= 0) {
            exception.setConstraintException(true);
        } else if (writeQuorumSize < ackQuorumSize || ensembleSize < writeQuorumSize) {
            exception.setConstraintException(true);
        } else {
            exception.setConstraintException(false);
        }

        if (mapType.equals(MapType.INVALID)) {
            exception.setCustomMetaException(true);
        } else {
            exception.setCustomMetaException(false);
        }

        return exception;
    }

    public static Map<String, byte[]> buildMap(MapType mapType) {
        Map<String, byte[]> map = null;
    
        switch (mapType) {
            case VALID:
                map = new HashMap<>();
                byte[] bytes = {1, 2, 3};
                map.put("customMeta", bytes);
                break;
            case EMPTY:
                map = new HashMap<>();
                break;
            case INVALID:
                map = mock(Map.class);
                when(map.get(anyString())).thenThrow(new RuntimeException("Invalid map"));
                break;
            default:
                break;
        }

        return map;
    }
}
