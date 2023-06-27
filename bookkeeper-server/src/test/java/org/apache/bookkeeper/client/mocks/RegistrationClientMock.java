package org.apache.bookkeeper.client.mocks;

import org.apache.bookkeeper.client.api.BKException;
import org.apache.bookkeeper.discover.RegistrationClient;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.utils.mocks.MockBehaviour;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.versioning.Version;
import org.apache.bookkeeper.versioning.Versioned;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistrationClientMock implements MockBehaviour {

    private RegistrationClient registrationClientMock = Mockito.mock(RegistrationClient.class);

    private List<BookieId> ensemble;
    private boolean isException;

    public RegistrationClientMock(List<BookieId> ensemble, boolean isException) {
        this.ensemble = ensemble;
        this.isException = isException;
    }

    @Override
    public MockBehaviour mock() throws MockException {

        if (!isException) {
            Mockito.when(registrationClientMock.getAllBookies()).thenReturn(getBookies(this.ensemble));
            Mockito.when(registrationClientMock.getWritableBookies()).thenReturn(getBookies(this.ensemble));
            Mockito.when(registrationClientMock.getReadOnlyBookies()).thenReturn(getBookies(this.ensemble));
        } else {
            this.registrationClientMock = null;
        }

        return this;
    }


    private CompletableFuture<Versioned<Set<BookieId>>> getBookies(List<BookieId> bookieIds) {
        CompletableFuture<Versioned<Set<BookieId>>> future = new CompletableFuture<>();

        // Create an empty set of BookieId
        Set<BookieId> bookies = new HashSet<>();

        // Add each BookieId to the set
        for (BookieId bookieId : bookieIds) {
            bookies.add(bookieId);
        }

        // Create a Versioned object with the set of bookies
        Versioned<Set<BookieId>> versionedBookies = new Versioned<>(bookies, Version.NEW);

        // Complete the CompletableFuture with the Versioned object
        future.complete(versionedBookies);

        return future;
    }

    public RegistrationClient getRegistrationClientMock() {
        return registrationClientMock;
    }
}
