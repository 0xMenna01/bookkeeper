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

    // Build parameters for two test iterations
    public static Collection<Object[]> buildParameters() {
        List<Object[]> parameters = new ArrayList<>();

        int i = 0;
        int[] ackQuorumSizeParams = {-1, 0, 1};
        for (int ackQuorumSize : ackQuorumSizeParams) {
            int[] writeQuorumSizeParams = {ackQuorumSize - 1, ackQuorumSize, ackQuorumSize + 1};
            for (int writeQuorumSize : writeQuorumSizeParams) {
                int[] ensembleSizeParams = {writeQuorumSize - 1, writeQuorumSize, writeQuorumSize + 1};
                for (int ensembleSize : ensembleSizeParams) {
                    InstanceType mapType = buildCustomMetaParam(i);

                    Object[] parameterSet = {ensembleSize, writeQuorumSize, ackQuorumSize, mapType};
                    parameters.add(parameterSet);

                    i++;
                }
            }
        }

        List<Object[]> secondIterTestParams = buildSecondTestIterationParams();

        Object[] params1 = {InstanceType.VALID, BookieIdxType.ZERO, InstanceType.EMPTY, false};
        Object[] params2 = {InstanceType.EMPTY, BookieIdxType.NEGATIVE, InstanceType.VALID, true};
        Object[] params3 = {InstanceType.NULL, BookieIdxType.ZERO, InstanceType.NULL, true};

        List<Object[]> finalParams = new ArrayList<>();
        int j = 0;

        for (i = 0; i < parameters.size(); i++) {
            Object[] params = parameters.get(i);

            if (i < 12) {
                Object[] secondIterParams = secondIterTestParams.get(i + 4);

                Object[] newParams = {params[0], params[1], params[2], params[3],
                    secondIterParams[0], secondIterParams[1], secondIterParams[2]};

                finalParams.add(newParams);
            } else if (i >= 12 && i < 16) {
                Object[] newParams = {params[0], params[1], params[2], params[3],
                    params1[0], params1[1], params1[2]};

                finalParams.add(newParams);

            } else if (i >= 16 && i < 20) {
                Object[] newParams = {params[0], params[1], params[2], params[3],
                    params2[0], params2[1], params2[2]};

                finalParams.add(newParams);

            } else if (i >= 20 && i < 23) {
                Object[] newParams = {params[0], params[1], params[2], params[3],
                    params3[0], params3[1], params3[2]};

                finalParams.add(newParams);

            } else if (i >= 23) {
                Object[] secondIterParams = secondIterTestParams.get(j);


                Object[] newParams = {params[0], params[1], params[2], params[3],
                    secondIterParams[0], secondIterParams[1], secondIterParams[2]};

                finalParams.add(newParams);

                j++;
            }

        }
        return finalParams;
    }


    private static InstanceType buildCustomMetaParam(int numTest) {
        InstanceType mapType;
        if (numTest <= 6) {
            mapType = InstanceType.NULL;
        } else if (numTest > 6 && numTest <= 12) {
            mapType = InstanceType.INVALID;
        } else if (numTest > 12 && numTest <= 19) {
            mapType = InstanceType.VALID;
        } else if (numTest > 19 && numTest <= 26) {
            mapType = InstanceType.EMPTY;
        } else {
            throw new RuntimeException("Number of arguments does not match the required");
        }

        return mapType;
    }

    public static ExceptionExpected buildMetaException(int ackQuorumSize, int writeQuorumSize, int ensembleSize, InstanceType mapType) {
        ExceptionExpected exception = new ExceptionExpected();

        if (ackQuorumSize <= 0 || writeQuorumSize <= 0 || ensembleSize <= 0) {
            exception.setConstraintException(true);
        } else if (writeQuorumSize < ackQuorumSize || ensembleSize < writeQuorumSize) {
            exception.setConstraintException(true);
        } else {
            exception.setConstraintException(false);
        }

        if (mapType.equals(InstanceType.INVALID)) {
            exception.setCustomMetaException(true);
        } else {
            exception.setCustomMetaException(false);
        }

        return exception;
    }

    public static Map<String, byte[]> buildMap(InstanceType mapType) {
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


    public static List<Object[]> buildSecondTestIterationParams() {
        List<Object[]> parameters = new ArrayList<>();

        for (InstanceType existingBookies : InstanceType.values()) {
            BookieIdxType[] bookieIdxs = {BookieIdxType.ZERO, BookieIdxType.NEGATIVE, BookieIdxType.LEN, BookieIdxType.MAX_LEN};
            InstanceType[] excludeBookiesArray = {InstanceType.VALID, InstanceType.NULL, InstanceType.EMPTY, InstanceType.INVALID};
            for (int j = 0; j < bookieIdxs.length; j++) {
                InstanceType excludeBookies = excludeBookiesArray[j];
                boolean skip = !isValidComb(existingBookies, excludeBookies);

                BookieIdxType index = bookieIdxs[j];
                if (skip && (bookieIdxs[j].equals(BookieIdxType.MAX_LEN) || bookieIdxs[j].equals(BookieIdxType.LEN))) {
                    index = BookieIdxType.ZERO;
                }

                Object[] parameterSet = {existingBookies, index, excludeBookies};
                parameters.add(parameterSet);

            }
        }

        return parameters;
    }

    public static boolean isReplaceBookiesException(InstanceType existingBookies, BookieIdxType index, InstanceType excludeBookies) {
        if (existingBookies.equals(InstanceType.VALID) &&
            (index.equals(BookieIdxType.ZERO) || index.equals(BookieIdxType.LEN)) &&
            !excludeBookies.equals(InstanceType.INVALID)) return false;

        return true;
    }

    public enum BookieIdxType {
        ZERO,
        NEGATIVE,
        LEN,
        MAX_LEN
    }


    private static boolean isValidComb(InstanceType existingBookies, InstanceType excludeBookies) {

        if (existingBookies.equals(InstanceType.NULL) ||
            existingBookies.equals(InstanceType.INVALID) ||
            existingBookies.equals(InstanceType.EMPTY)
        ) return false;

        return true;
    }
}
