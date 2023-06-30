package org.apache.bookkeeper.tls.mocks;

import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.utils.mocks.MockBehaviour;
import org.mockito.Mockito;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.security.auth.x500.X500Principal;

public class CertificatesMock implements MockBehaviour {

    CertMeta certMeta;
    X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
    Collection<Object> mockCertificates = null;


    public CertificatesMock(CertMeta certMeta) {
        this.certMeta = certMeta;
    }

    @Override
    public CertificatesMock mock() throws MockException {

        switch (certMeta.getInstance()) {
            case VALID:
                mockValidCertificates();
                break;
            case WRONG_ROLES:
                mockValidCertificates();
                break;
            case NULL:
                this.mockX509Certificate = null;
                break;
            default:
                mockInvalidCertificates();
        }

        return this;
    }


    private void mockValidCertificates() {
        setCertificates();

        String certRole = TestUtils.buildCertRole(certMeta.getCertRole());

        X500Principal mockX500Principal = new X500Principal(certRole);
        Mockito.when(mockX509Certificate.getSubjectX500Principal())
            .thenReturn(mockX500Principal);
    }

    
    private void setCertificates() {
        this.mockCertificates = Arrays.asList(mockX509Certificate);
    }

    /// An *INVALID* collection of certificates will have mockX509Certificate without a X500Principal
    /// The call to mockX509Certificate.getSubjectX500Principal() must throw an exception
    private void mockInvalidCertificates() {
        setCertificates();
    }

    public Collection<Object> getMockCertificates() {
        return mockCertificates;
    }

}
