package com.ionic.sdk.addon.jdbc.usecase2.employee;

/**
 * Extension of {@link Employee} that also contains Ionic-protected representations of sensitive fields.
 * <p>
 * The Ionic-protected representation of a data value includes several components:
 * <ul>
 * <li>the Ionic key id associated with the ciphertext</li>
 * <li>a random cryptographic initialization vector (IV) used to encrypt the plaintext</li>
 * <li>the encrypted form of the plaintext itself</li>
 * </ul>
 * <p>
 * Ionic chunk cryptography formats such as {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2} package these
 * components into a single text string, using the base64 scheme to encode the binary ciphertext.
 * <p>
 * For this use case, we are assuming that the Employee database table schema is locked and may not be altered.  We
 * need additional space to store the associated Ionic metadata.  The solution is to add another database table to hold
 * the Ionic content, with a foreign key relationship to the Employee table id.  For simplicity, rather than store the
 * ciphertext components separately, they are encoded into a single
 * {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2} string, and stored as a database VARCHAR.
 */
public class EmployeeIonic extends Employee {

    /**
     * The Ionic representations of the protected record fields.
     */
    private final EmployeeIonicFields ionicFields;

    /**
     * Constructor.
     *
     * @param employee    the native employee record
     * @param ionicFields the Ionic-protected representation of the sensitive employee fields
     */
    public EmployeeIonic(final Employee employee, final EmployeeIonicFields ionicFields) {
        super(employee.getId(), employee.getFirstName(), employee.getLastName(),
                DEFAULT_VALUE_PERSONAL_IDENTIFIER, DEFAULT_VALUE_SALARY, employee.getCountry());
        this.ionicFields = ionicFields;
    }

    /**
     * @return the Ionic representations of the protected record fields
     */
    public EmployeeIonicFields getIonicFields() {
        return ionicFields;
    }

    private static final String DEFAULT_VALUE_PERSONAL_IDENTIFIER = "000000000";
    private static final int DEFAULT_VALUE_SALARY = 0;
}
