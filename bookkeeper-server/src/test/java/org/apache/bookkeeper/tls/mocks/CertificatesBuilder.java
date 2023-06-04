package org.apache.bookkeeper.tls.mocks;

import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.tls.utils.enums.ConfigType;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;

public class CertificatesBuilder implements MockBuilder<CertificatesMock>{

   private GenericInstance instanceType;
   private static CertificatesBuilder instance = null;

    private CertificatesBuilder(){}

    public static CertificatesBuilder getInstance() {
        if(instance == null) {
            instance = new CertificatesBuilder();
        }
        return instance;
    }

    @Override
    public void setup(GenericInstance instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public CertificatesMock build() throws MockException {
        CertMeta certMeta = new CertMeta();
        String[] certRole = null;

        switch (instanceType) {
            case VALID:
                certRole = TestUtils.buildCertRole(ConfigType.VALID_SINGLE_ROLE);
                break;
            case INVALID:
                certRole = TestUtils.buildCertRole(ConfigType.INVALID);
                break;
            case NULL:
                break;

        }
        certMeta.setMeta(instanceType, certRole);

        return new CertificatesMock(certMeta);
    }
}
