package org.apache.bookkeeper.client;

import static org.apache.bookkeeper.client.TopologyAwareEnsemblePlacementPolicy.REPP_DNS_RESOLVER_CLASS;
import static org.apache.bookkeeper.feature.SettableFeatureProvider.DISABLE_ALL;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.bookkeeper.client.mocks.RegistrationClientMock;
import org.apache.bookkeeper.client.utils.ExceptionExpected;
import org.apache.bookkeeper.client.utils.InstanceType;
import org.apache.bookkeeper.client.utils.InstancesReplaceBookie;
import org.apache.bookkeeper.client.utils.TestClientUtils;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.discover.RegistrationClient;
import org.apache.bookkeeper.meta.MetadataDrivers;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.bookkeeper.net.DNSToSwitchMapping;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.util.StaticDNSResolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;

@RunWith(Parameterized.class)
public class BookieWatcherImplForMutationTest {

    private ClientConfiguration conf = new ClientConfiguration();
    private List<BookieId> ensemble = new ArrayList<>();

    private DefaultEnsemblePlacementPolicy defaultPolicy;
    private BookieWatcherImpl bookieWatcher;

    private int ensembleSize;
    private int writeQuorumSize;
    private int ackQuorumSize;
    private Map<String, byte[]> customMetadata;
    private List<BookieId> existingBookies;
    private int bookieIdx;
    private Set<BookieId> excludeBookies;
    private boolean isBkException;

    // These tests aim to increase pit mutation score, by killing introduced mutant.
    // The strategy will be the following:
    // 1) construct a WatcherImpl instance with an invalid ensemble to properly launch a BKException
    // 2) verify the execution results of less relevant methods not covered by previous test iteration
    //    based on a RegistrationClient mock

    public BookieWatcherImplForMutationTest(int ensembleSize,
                                            int writeQuorumSize,
                                            int ackQuorumSize,
                                            Map<String, byte[]> customMetadata,
                                            InstanceType existingBookies,
                                            int bookieIdx,
                                            InstanceType excludeBookies,
                                            boolean isBkException) throws Exception {

        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.customMetadata = customMetadata;
        this.isBkException = isBkException;
        this.bookieIdx = bookieIdx;

        if (!existingBookies.equals(InstanceType.VALID) || !excludeBookies.equals(InstanceType.EMPTY)) {
            throw new RuntimeException("This test iteration only supports valid instances of existingbookies " +
                "and empty instances of excludeBookies");
        }

        setUp();

        RegistrationClientMock registrationClientMock = (RegistrationClientMock) new RegistrationClientMock(this.ensemble, isBkException).mock();


        this.bookieWatcher = new BookieWatcherImpl(
            conf,
            defaultPolicy,
            registrationClientMock.getRegistrationClientMock(),
            BookieSocketAddress.LEGACY_BOOKIEID_RESOLVER,
            NullStatsLogger.INSTANCE
        );


    }

    private void setUp() {
        StaticDNSResolver.reset();

        conf.setProperty(REPP_DNS_RESOLVER_CLASS, StaticDNSResolver.class.getName());

        BookieSocketAddress addr1 = new BookieSocketAddress("127.0.0.2", 3181);
        BookieSocketAddress addr2 = new BookieSocketAddress("127.0.0.3", 3181);
        BookieSocketAddress addr3 = new BookieSocketAddress("127.0.0.4", 3181);
        ensemble.add(addr1.toBookieId());
        ensemble.add(addr2.toBookieId());
        ensemble.add(addr3.toBookieId());


        HashedWheelTimer timer = new HashedWheelTimer(
            new ThreadFactoryBuilder().setNameFormat("TestTimer-%d").build(),
            conf.getTimeoutTimerTickDurationMs(), TimeUnit.MILLISECONDS,
            conf.getTimeoutTimerNumTicks());

        defaultPolicy = new DefaultEnsemblePlacementPolicy();
        defaultPolicy.initialize(conf, Optional.<DNSToSwitchMapping>empty(), timer,
            DISABLE_ALL, NullStatsLogger.INSTANCE, BookieSocketAddress.LEGACY_BOOKIEID_RESOLVER);

        if (!isBkException) {
            Set<BookieId> addrs = new HashSet<BookieId>();
            addrs.add(addr1.toBookieId());
            addrs.add(addr2.toBookieId());
            addrs.add(addr3.toBookieId());

            defaultPolicy.onClusterChanged(addrs, new HashSet<BookieId>());
        }

        this.existingBookies = ensemble.subList(0, 1);
        this.excludeBookies = new HashSet<BookieId>();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
            //E   Qw   Qa   customMetadata   existingBookies   bookieIdx   excludeBookies   BKEXCEPTION
            {1, 1, 1, null, InstanceType.VALID, 0, InstanceType.EMPTY, false},
            {1, 1, 1, null, InstanceType.VALID, 0, InstanceType.EMPTY, true},
        });
    }

    @Test
    public void testNewEnsemble() {

        try {
            List<BookieId> bookieIds = bookieWatcher.newEnsemble(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata);

            Assert.assertFalse("An exception was expected.\n", isBkException);

            Assert.assertEquals("The number of instances of type BookieId does not match the expected", bookieIds.size(), ensembleSize);

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown.\n",
                isBkException);
        }
    }

    @Test
    public void testReplaceBookie() {

        try {
            BookieId bookieId = bookieWatcher.replaceBookie(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata,
                existingBookies, bookieIdx, excludeBookies);

            Assert.assertFalse("An exception was expected.\n", isBkException);

            Assert.assertTrue("The candidate bookie is not in the ensamble", ensemble.contains(bookieId));

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                ", but " + e.getClass().getName() + " has been thrown.\n", isBkException);
        }
    }


    @Test
    public void testRegistrationClientReadOnlyBookies() {

        try {
            List<BookieId> bookies = new ArrayList<>(bookieWatcher.getReadOnlyBookies());
            Assert.assertEquals("List of bookies must be equal.\n", this.ensemble, bookies);

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                ", but " + e.getClass().getName() + " has been thrown.\n", isBkException);
        }
    }

    @Test
    public void testRegistrationClientGetBookies() {

        try {
            List<BookieId> bookies = new ArrayList<>(bookieWatcher.getBookies());

            Assert.assertEquals("List of bookies must be equal.\n", this.ensemble, bookies);

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                ", but " + e.getClass().getName() + " has been thrown.\n", isBkException);
        }
    }

    @Test
    public void testRegistrationClientGetAllBookies() {

        try {
            List<BookieId> bookies = new ArrayList<>(bookieWatcher.getAllBookies());

            Assert.assertEquals("List of bookies must be equal.\n", this.ensemble, bookies);

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                ", but " + e.getClass().getName() + " has been thrown.\n", isBkException);
        }
    }


    @After
    public void tearDown() {
        defaultPolicy.uninitalize();
    }


}
