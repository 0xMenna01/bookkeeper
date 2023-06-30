package org.apache.bookkeeper.tls.mocks;

import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.utils.GenericInstance;
import org.apache.bookkeeper.utils.mocks.MockException;

import java.util.Arrays;
import java.util.List;

public class CertMeta {
    private TestUtils.ConnectionPeerType instance;
    private String[] certRole;

    public CertMeta() {
    }

    public void setMeta(TestUtils.ConnectionPeerType instance, String[] certRole) throws MockException {
        switch (instance) {
            case NULL:
                if (certRole == null)
                    break;
            case VALID:
                List<String> validRoles = listRoles(instance, certRole);
                if (isValid(validRoles)) break;
            case WRONG_ROLES:
                List<String> wrongRoles = listRoles(instance, certRole);
                if (isValid(wrongRoles)) break;
            default:
                List<String> invalidRoles = listRoles(instance, certRole);
                if (!isValid(invalidRoles))
                    break;
                error();
        }

        this.instance = instance;
        this.certRole = certRole;
    }

    private boolean isValid(List<String> roleList) {
        return !(roleList.isEmpty() || roleList.contains("") || roleList.contains(","));
    }

    private List<String> listRoles(TestUtils.ConnectionPeerType instance, String[] certRole) throws MockException {
        if (certRole == null) throw new MockException("Roles must NOT be null");

        return Arrays.asList(certRole);
    }

    private void error() throws MockException {
        throw new MockException("The provided mock is not valid");
    }

    public TestUtils.ConnectionPeerType getInstance() {
        return instance;
    }

    public String[] getCertRole() {
        return certRole;
    }
}
