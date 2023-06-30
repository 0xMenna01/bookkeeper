package org.apache.bookkeeper.tls.mocks.builders;

import org.apache.bookkeeper.tls.mocks.CertMeta;
import org.apache.bookkeeper.tls.mocks.CertificatesMock;
import org.apache.bookkeeper.utils.GenericInstance;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.tls.utils.ConfigType;
import org.apache.bookkeeper.utils.mocks.GenericMockBuilder;

public class CertificatesBuilder extends GenericMockBuilder<CertificatesMock, TestUtils.ConnectionPeerType> {

    private static CertificatesBuilder instance = null;

    private CertificatesBuilder() {
    }

    public static CertificatesBuilder getInstance() {
        if (instance == null) {
            instance = new CertificatesBuilder();
        }
        return instance;
    }

    @Override
    public CertificatesMock build() throws MockException {
        CertMeta certMeta = new CertMeta();
        String[] certRole = null;

        switch (this.instanceType) {
            case VALID:
                certRole = TestUtils.getRoles(ConfigType.VALID_SINGLE_ROLE);
                break;
            case WRONG_ROLES:
                certRole = TestUtils.getRoles(ConfigType.VALID_MULTIPLE_ROLES);
                break;
            case NULL:
                break;
            default:
                certRole = TestUtils.getRoles(ConfigType.EMPTY);
                break;
        }

        certMeta.setMeta(this.instanceType, certRole);

        return new CertificatesMock(certMeta);
    }
}
