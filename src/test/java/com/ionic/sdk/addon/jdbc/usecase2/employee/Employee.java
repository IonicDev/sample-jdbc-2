package com.ionic.sdk.addon.jdbc.usecase2.employee;

/**
 * Object representation of employee record, held in database table "employee".  No business logic, just
 * typed data values.
 */
public class Employee {

    /**
     * Database id.  Value of zero indicates an employee that is not bound to the database table.
     */
    private final int id;

    /**
     * The first name of the employee.
     */
    private final String firstName;

    /**
     * The last name of the employee.
     */
    private final String lastName;

    /**
     * The personal identifier for the record.  This data is a stand-in for a PII value (such as a social security
     * number), and is normalized to nine data characters (dash characters are removed).
     */
    private final String personalIdentifier;  // DB limited to 9 characters (normalized)

    /**
     * The salary for the employee.  This data is a non-textual PII value, and is used to illustrate how integral
     * values may be Ionic-protected at rest.
     */
    private final int salary;

    /**
     * The residency country of the employee.
     */
    private final String country;

    /**
     * Constructor.
     *
     * @param id                 the database id
     * @param firstName          the employee first name
     * @param lastName           the employee last name
     * @param personalIdentifier the employee personal identifier
     * @param salary             the employee salary
     * @param country            the employee residency country
     */
    public Employee(final int id, final String firstName, final String lastName,
                    final String personalIdentifier, final int salary, final String country) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalIdentifier = personalIdentifier;
        this.salary = salary;
        this.country = country;
    }

    public Employee(final Employee employee) {
        this.id = employee.getId();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.personalIdentifier = employee.getPersonalIdentifier();
        this.salary = employee.getSalary();
        this.country = employee.getCountry();
    }

    /**
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the employee first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the employee last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the employee personal identifier
     */
    public String getPersonalIdentifier() {
        return personalIdentifier;
    }

    /**
     * @return the employee salary
     */
    public int getSalary() {
        return salary;
    }

    /**
     * @return the employee residency country
     */
    public String getCountry() {
        return country;
    }
}
