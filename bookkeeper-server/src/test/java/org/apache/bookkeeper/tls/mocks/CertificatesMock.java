package org.apache.bookkeeper.tls.mocks;
import org.apache.bookkeeper.util.CertUtils;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;

import javax.security.auth.x500.X500Principal;

public class CertificatesMock implements MockBehaviour {


    CertMeta certMeta;
    Collection<Object> mockCertificates = Mockito.mock(Collection.class);
    X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);


    public CertificatesMock(CertMeta certMeta) {
        this.certMeta = certMeta;
    }

    @Override
    public CertificatesMock mock() throws MockException {

        switch (certMeta.getInstance()) {
            case VALID:
                mockValidCertificates();
                break;
            case INVALID:
                Mockito.when(mockCertificates.isEmpty()).thenReturn(true);
                break;
            case NULL:
                this.mockCertificates = null;
                this.mockX509Certificate = null;
                break;

            default:
                throw new MockException("Invalid Certification instance");
        }

        return this;
    }

    private void mockValidCertificates() {
        Mockito.when(mockCertificates.isEmpty()).thenReturn(false);
        Mockito.when(mockCertificates.iterator())
            .thenReturn(Collections.singleton((Object) mockX509Certificate).iterator());

        X500Principal mockX500Principal = new X500Principal(certMeta.getCertRole()[0]);
        Mockito.when(mockX509Certificate.getSubjectX500Principal())
            .thenReturn(mockX500Principal);
    }

    public Collection<Object> getMockCertificates() {
        return mockCertificates;
    }

    public X509Certificate getMockX509Certificate() {
        return mockX509Certificate;
    }
}
