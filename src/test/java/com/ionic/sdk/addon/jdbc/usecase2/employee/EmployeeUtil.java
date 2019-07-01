package com.ionic.sdk.addon.jdbc.usecase2.employee;

import com.ionic.sdk.addon.jdbc.usecase2.jdbc.IonicTypes;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to generate random employee records for use in testing.
 */
public class EmployeeUtil {

    /**
     * @return a newly fabricated employee record, containing random data
     */
    public static Employee generate() {
        final String firstName = getFirstName();
        final String lastName = getLastName();
        return new Employee(0, firstName, lastName,
                getPersonalIdentifier(firstName, lastName),
                getSalary(), getCountryCode());
    }

    /**
     * Test utility method.
     *
     * @return a random common first name
     */
    private static String getFirstName() {
        // https://www.ssa.gov/oact/babynames/decades/century.html
        final List<String> firstNames = Arrays.asList(
                "James", "Mary", "John", "Patricia", "Robert",
                "Jennifer", "Michael", "Linda", "William", "Elizabeth",
                "David", "Barbara", "Richard", "Susan", "Joseph",
                "Jessica", "Thomas", "Sarah", "Charles", "Karen");
        Collections.shuffle(firstNames);
        return firstNames.iterator().next();
    }

    /**
     * Test utility method.
     *
     * @return a random common last name
     */
    private static String getLastName() {
        // https://en.wikipedia.org/wiki/List_of_most_common_surnames_in_North_America#United_States_(American)
        final List<String> lastNames = Arrays.asList(
                "Smith", "Johnson", "Williams", "Brown", "Jones",
                "Miller", "Davis", "Garcia", "Rodriguez", "Wilson",
                "Martinez", "Anderson", "Taylor", "Thomas", "Hernandez",
                "Moore", "Martin", "Jackson", "Thompson", "White");
        Collections.shuffle(lastNames);
        return lastNames.iterator().next();
    }

    /**
     * Test utility method.
     * <p>
     * The generation of a new employee record includes a personal identifier, which is then encrypted.  The plaintext
     * personal identifier is never stored.  So an out-of-band means of verifying the decryption of the personal
     * identifier is needed.  The strategy in use here is to derive the identifier from data which is not protected.
     *
     * @param firstName input to the identifier hash calculation
     * @param lastName  input to the identifier hash calculation
     * @return a recoverable personal identifier
     */
    public static String getPersonalIdentifier(final String firstName, final String lastName) {
        final byte[] bytes = CryptoUtils.sha256ToBytes(Transcoder.utf8().decode(firstName + lastName));
        final int value = IonicTypes.toInt(bytes);
        return Integer.toString((value & Integer.MAX_VALUE) % 1000000000);
        //final int personalIdentifier = new Random().nextInt(1000000000);
        //return Integer.toString(personalIdentifier);
    }

    /**
     * Test utility method.  The value is hard-coded to enable verification on decryption.
     *
     * @return a integer salary
     */
    private static int getSalary() {
        return SALARY;
    }

    /**
     * The hard-coded value of a sensitive data field, used to enable verification of data decryption.
     */
    public static final int SALARY = 50000;

    /**
     * Test utility method.
     *
     * @return a random common last name
     */
    private static String getCountryCode() {
        final List<String> countryCodes = Arrays.asList(
                "US", "UK", "ES", "FR", "DE");
        Collections.shuffle(countryCodes);
        return countryCodes.iterator().next();
    }
}
