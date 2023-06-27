package org.apache.bookkeeper.client.utils;

public class InstancesReplaceBookie {

    private InstanceType existingBookies;
    private TestClientUtils.BookieIdxType bookieIdxType;
    private InstanceType excludeBookies;

    public InstancesReplaceBookie(InstanceType existingBookies, TestClientUtils.BookieIdxType bookieIdxType, InstanceType excludeBookies) {
        this.existingBookies = existingBookies;
        this.bookieIdxType = bookieIdxType;
        this.excludeBookies = excludeBookies;
    }


    public InstanceType getExistingBookiesType() {
        return existingBookies;
    }

    public TestClientUtils.BookieIdxType getBookieIdxType() {
        return bookieIdxType;
    }

    public InstanceType getExcludeBookiesType() {
        return excludeBookies;
    }
}
