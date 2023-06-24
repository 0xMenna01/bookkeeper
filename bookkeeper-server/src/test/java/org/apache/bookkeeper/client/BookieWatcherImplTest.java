package org.apache.bookkeeper.client;

import static org.apache.bookkeeper.client.RackawareEnsemblePlacementPolicyImpl.REPP_DNS_RESOLVER_CLASS;
import static org.apache.bookkeeper.feature.SettableFeatureProvider.DISABLE_ALL;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.util.HashedWheelTimer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.bookkeeper.client.utils.ExceptionExpected;
import org.apache.bookkeeper.client.utils.MapType;
import org.apache.bookkeeper.client.utils.TestClientUtils;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.bookkeeper.net.DNSToSwitchMapping;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.util.StaticDNSResolver;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class BookieWatcherImplTest {
    private static final Logger LOG = LoggerFactory.getLogger(BookieWatcherImplTest.class);
    private ClientConfiguration conf = new ClientConfiguration();

    // Classes for integration test
    private DefaultEnsemblePlacementPolicy defaultPolicy;
    private BookieWatcherImpl bookieWatcher;

    // Test parameters using category partition
    private int ensembleSize;
    private int writeQuorumSize;
    private int ackQuorumSize;
    private Map<String, byte[]> customMetadata;
    private MapType mapType;

    //Expected exception
    private ExceptionExpected exceptionExpected;

    public BookieWatcherImplTest(int ensembleSize,
                                 int writeQuorumSize,
                                 int ackQuorumSize,
                                 MapType mapType,
                                 ExceptionExpected exceptionExpected) throws Exception {
        setUpPolicy();

        this.bookieWatcher = new BookieWatcherImpl(
            conf,
            defaultPolicy,
            null,
            BookieSocketAddress.LEGACY_BOOKIEID_RESOLVER,
            NullStatsLogger.INSTANCE
        );


        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.mapType = mapType;
        this.customMetadata = TestClientUtils.buildMap(mapType);

        this.exceptionExpected = exceptionExpected;
    }

    // Set up an instance of type DefaultEnsemblePlacementPolicy
    private void setUpPolicy() throws Exception {
        StaticDNSResolver.reset();

        conf.setProperty(REPP_DNS_RESOLVER_CLASS, StaticDNSResolver.class.getName());

        BookieSocketAddress addr1 = new BookieSocketAddress("127.0.0.2", 3181);
        BookieSocketAddress addr2 = new BookieSocketAddress("127.0.0.3", 3181);
        BookieSocketAddress addr3 = new BookieSocketAddress("127.0.0.4", 3181);
        BookieSocketAddress addr4 = new BookieSocketAddress("127.0.0.5", 3181);

        HashedWheelTimer timer = new HashedWheelTimer(
            new ThreadFactoryBuilder().setNameFormat("TestTimer-%d").build(),
            conf.getTimeoutTimerTickDurationMs(), TimeUnit.MILLISECONDS,
            conf.getTimeoutTimerNumTicks());

        defaultPolicy = new DefaultEnsemblePlacementPolicy();
        defaultPolicy.initialize(conf, Optional.<DNSToSwitchMapping>empty(), timer,
            DISABLE_ALL, NullStatsLogger.INSTANCE, BookieSocketAddress.LEGACY_BOOKIEID_RESOLVER);

        Set<BookieId> addrs = new HashSet<BookieId>();
        addrs.add(addr1.toBookieId());
        addrs.add(addr2.toBookieId());
        addrs.add(addr3.toBookieId());
        addrs.add(addr4.toBookieId());

        defaultPolicy.onClusterChanged(addrs, new HashSet<BookieId>());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return TestClientUtils.buildParameters();
    }


    @Test
    public void testNewEnsemble() {

        LOG.info("\nensemble: " + ensembleSize + "\n"
            + "write quorum: " + writeQuorumSize + "\n"
            + "ack quorum: " + ackQuorumSize + "\n"
            + "map type: " + mapType.toString() + "\n"
        );

        try {
            List<BookieId> bookieIds = bookieWatcher.newEnsemble(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata);
            Assert.assertFalse("An exception was expected.\n" +
                exceptionExpected.toString(), exceptionExpected.shouldThrow());

            Assert.assertEquals("The number of instances of type BookieId does not match the expected", bookieIds.size(), ensembleSize);

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown.\n" +
                    exceptionExpected.toString(),
                exceptionExpected.shouldThrow());
        }
    }

    @After
    public void tearDown() {
        defaultPolicy.uninitalize();
    }
}