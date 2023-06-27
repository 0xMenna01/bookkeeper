package org.apache.bookkeeper.client;

import static org.apache.bookkeeper.client.RackawareEnsemblePlacementPolicyImpl.REPP_DNS_RESOLVER_CLASS;
import static org.apache.bookkeeper.feature.SettableFeatureProvider.DISABLE_ALL;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.util.HashedWheelTimer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.bookkeeper.client.utils.ExceptionExpected;
import org.apache.bookkeeper.client.utils.InstanceType;
import org.apache.bookkeeper.client.utils.InstancesReplaceBookie;
import org.apache.bookkeeper.client.utils.TestClientUtils;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.meta.MetadataDrivers;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.bookkeeper.net.DNSToSwitchMapping;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.util.StaticDNSResolver;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class BookieWatcherImplTest {
    private static final Logger LOG = LoggerFactory.getLogger(BookieWatcherImplTest.class);
    private ClientConfiguration conf = new ClientConfiguration();
    private List<BookieId> ensemble = new ArrayList<>();

    // Classes for integration test
    private DefaultEnsemblePlacementPolicy defaultPolicy;
    private BookieWatcherImpl bookieWatcher;

    // Test parameters using category partition for both first and second iteration
    private int ensembleSize;
    private int writeQuorumSize;
    private int ackQuorumSize;
    private Map<String, byte[]> customMetadata;
    private InstanceType mapType;

    //Expected exception for metadata constraint
    private ExceptionExpected exceptionConstraintAndMeta;

    // Second iteration only
    private List<BookieId> existingBookies;
    private int bookieIdx;
    private Set<BookieId> excludeBookies;
    private InstancesReplaceBookie instances;
    private boolean exceptionSecondIter;


    public BookieWatcherImplTest(int ensembleSize,
                                 int writeQuorumSize,
                                 int ackQuorumSize,
                                 InstanceType mapType,
                                 InstanceType existingBookies,
                                 TestClientUtils.BookieIdxType bookieIdx,
                                 InstanceType excludeBookies) throws Exception {

        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.mapType = mapType;
        this.customMetadata = TestClientUtils.buildMap(mapType);

        this.exceptionConstraintAndMeta = TestClientUtils.buildMetaException(ackQuorumSize, writeQuorumSize, ensembleSize, mapType);

        //Only for second iteration
        this.instances = new InstancesReplaceBookie(existingBookies, bookieIdx, excludeBookies);
        this.exceptionSecondIter = TestClientUtils.isReplaceBookiesException(existingBookies, bookieIdx, excludeBookies);

        setUpPolicy();
        setUpBookies();
        setUpIndex();

        this.bookieWatcher = new BookieWatcherImpl(
            conf,
            defaultPolicy,
            null,
            BookieSocketAddress.LEGACY_BOOKIEID_RESOLVER,
            NullStatsLogger.INSTANCE
        );
    }

    // Set up an instance of type DefaultEnsemblePlacementPolicy
    private void setUpPolicy() {
        StaticDNSResolver.reset();

        if (!exceptionSecondIter) {
            conf.setDiskWeightBasedPlacementEnabled(true);
        }

        conf.setProperty(REPP_DNS_RESOLVER_CLASS, StaticDNSResolver.class.getName());

        BookieSocketAddress addr1 = new BookieSocketAddress("127.0.0.2", 3181);
        BookieSocketAddress addr2 = new BookieSocketAddress("127.0.0.3", 3181);
        BookieSocketAddress addr3 = new BookieSocketAddress("127.0.0.4", 3181);
        BookieSocketAddress addr4 = new BookieSocketAddress("127.0.0.5", 3181);
        BookieSocketAddress addr5 = new BookieSocketAddress("127.0.0.6", 3181);
        ensemble.add(addr1.toBookieId());
        ensemble.add(addr2.toBookieId());
        ensemble.add(addr3.toBookieId());
        ensemble.add(addr4.toBookieId());
        ensemble.add(addr4.toBookieId());
        ensemble.add(addr5.toBookieId());

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
        addrs.add(addr5.toBookieId());

        defaultPolicy.onClusterChanged(addrs, new HashSet<BookieId>());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return TestClientUtils.buildParameters();
    }


    private void setUpBookies() {
        switch (this.instances.getExistingBookiesType()) {
            case VALID:
                this.existingBookies = ensemble.subList(0, 4);
                break;
            case EMPTY:
                this.existingBookies = new ArrayList<BookieId>();
                break;
            case INVALID:
                this.existingBookies = Mockito.mock(List.class);
                Mockito.when(this.existingBookies.size()).thenThrow(new RuntimeException("Invalid list of bookies"));
                break;
            case NULL:
                this.existingBookies = null;
                break;
        }

        switch (this.instances.getExcludeBookiesType()) {
            case VALID:
                BookieId excludeBookie = ensemble.get(4);
                Set<BookieId> bookiesSet = new HashSet<>();
                bookiesSet.add(excludeBookie);
                this.excludeBookies = bookiesSet;
                break;
            case EMPTY:
                this.excludeBookies = new HashSet<>();
                break;
            case INVALID:
                BookieSocketAddress invalidBookieAddr = new BookieSocketAddress("127.0.0.21", 3181);
                Set<BookieId> invalidSet = new HashSet<>();
                invalidSet.add(invalidBookieAddr.toBookieId());
                this.excludeBookies = invalidSet;
                break;
            case NULL:
                this.excludeBookies = null;
                break;
        }
    }


    private void setUpIndex() {
        switch (this.instances.getBookieIdxType()) {
            case ZERO:
                this.bookieIdx = 0;
                break;
            case NEGATIVE:
                this.bookieIdx = -1;
                break;
            case LEN:
                this.bookieIdx = this.existingBookies.size() - 1;
                break;
            case MAX_LEN:
                this.bookieIdx = this.existingBookies.size();
                break;
        }
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

            Assume.assumeFalse("An exception was expected.\n" +
                exceptionConstraintAndMeta.toString(), exceptionConstraintAndMeta.shouldThrow());

            Assert.assertFalse("An exception was expected.\n" +
                exceptionConstraintAndMeta.toString(), exceptionConstraintAndMeta.shouldThrow());

            Assert.assertEquals("The number of instances of type BookieId does not match the expected", bookieIds.size(), ensembleSize);

        } catch (Exception e) {
            Assume.assumeTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown.\n" +
                    exceptionConstraintAndMeta.toString(),
                exceptionConstraintAndMeta.shouldThrow());
            Assert.assertTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown.\n" +
                    exceptionConstraintAndMeta.toString(),
                exceptionConstraintAndMeta.shouldThrow());
        }
    }

    @Test
    public void testReplaceBookie() {

        LOG.info("\nensemble: " + ensembleSize + "\n"
            + "write quorum: " + writeQuorumSize + "\n"
            + "ack quorum: " + ackQuorumSize + "\n"
            + "map type: " + mapType.toString() + "\n"
            + "existing bookies: " + this.instances.getExistingBookiesType().toString() + "\n"
            + "bookie index: " + this.instances.getBookieIdxType().toString() + "\n"
            + "exclude bookies: " + this.instances.getExcludeBookiesType().toString() + "\n"
        );

        try {
            BookieId bookieId = bookieWatcher.replaceBookie(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata,
                existingBookies, bookieIdx, excludeBookies);

            Assume.assumeFalse("An exception was expected.\n", exceptionSecondIter || exceptionConstraintAndMeta.shouldThrow());

            Assert.assertFalse("An exception was expected.\n", exceptionSecondIter || exceptionConstraintAndMeta.shouldThrow());

            Assert.assertTrue("The candidate bookie is not in the ensamble", ensemble.contains(bookieId));

        } catch (Exception e) {
            Assume.assumeTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown.\n",
                exceptionSecondIter || exceptionConstraintAndMeta.shouldThrow());
            Assert.assertTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown.\n",
                exceptionSecondIter || exceptionConstraintAndMeta.shouldThrow());
        }
    }


    @After
    public void tearDown() {
        defaultPolicy.uninitalize();
    }
}