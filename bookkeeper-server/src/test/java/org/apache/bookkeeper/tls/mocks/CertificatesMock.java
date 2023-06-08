package org.apache.bookkeeper.tls.mocks;

import org.apache.bookkeeper.tls.utils.enums.GenericInstance;
import org.apache.bookkeeper.util.CertUtils;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CertificatesMock implements MockBehaviour {


    CertMeta certMeta;
    Collection<Object> mockCertificates = Mockito.mock(Collection.class);
    X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
    CertUtils mockCertUtils = Mockito.mock(CertUtils.class);


    public CertificatesMock(CertMeta certMeta) {
        this.certMeta = certMeta;
    }

    @Override
    public void mock() throws MockException {

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
    }

    private void mockValidCertificates() {
        Mockito.when(mockCertificates.isEmpty()).thenReturn(false);
        Mockito.when(mockCertificates.iterator())
            .thenReturn(Collections.singleton((Object) mockX509Certificate).iterator());
        try {
            Mockito.when(mockCertUtils.getRolesFromOU(mockX509Certificate)).thenReturn(certMeta.getCertRole());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Object> getMockCertificates() {
        return mockCertificates;
    }

    public CertUtils getMockCertUtils() {
        return mockCertUtils;
    }
}
