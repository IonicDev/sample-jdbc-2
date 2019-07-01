package com.ionic.sdk.addon.jdbc.usecase2.employee;

import com.ionic.sdk.addon.jdbc.usecase2.jdbc.IonicTypes;
import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2;
import com.ionic.sdk.error.IonicException;

/**
 * Utility class for conversion of Employee record to/from Ionic-protected representation.
 */
public class EmployeeIonicUtil {

    /**
     * Calculate Ionic-protected representations of sensitive employee fields.
     *
     * @param employee the employee needing to be Ionic-protected
     * @param agent    the Ionic key services implementation; used to provide keys for cryptography operations
     * @return an object holding the Ionic-protected values
     * @throws IonicException on cryptography failures
     */
    public static EmployeeIonicFields toEmployeeIonicFields(
            final Employee employee, final Agent agent) throws IonicException {
        final ChunkCipherV2 cipher = new ChunkCipherV2(agent);
        return new EmployeeIonicFields(employee.getId(),
                cipher.encrypt(employee.getPersonalIdentifier()),
                cipher.encrypt(IonicTypes.toBytes(employee.getSalary())));
    }

    /**
     * Unprotect Ionic representation of an employee record.
     *
     * @param employeeIonic the employee needing to be Ionic-unprotected
     * @param agent         the Ionic key services implementation; used to provide keys for cryptography operations
     * @return an object holding the plaintext values
     * @throws IonicException on cryptography failures
     */
    public static Employee toEmployee(
            final EmployeeIonic employeeIonic, final Agent agent) throws IonicException {
        final ChunkCipherV2 cipher = new ChunkCipherV2(agent);
        return new Employee(employeeIonic.getId(), employeeIonic.getFirstName(), employeeIonic.getLastName(),
                cipher.decrypt(employeeIonic.getIonicFields().getPersonalIdentifier()),
                IonicTypes.toInt(cipher.decryptToBytes(employeeIonic.getIonicFields().getSalary())),
                employeeIonic.getCountry());
    }
}
