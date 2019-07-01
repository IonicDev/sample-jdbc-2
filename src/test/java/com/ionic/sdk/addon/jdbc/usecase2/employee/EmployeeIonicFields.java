package com.ionic.sdk.addon.jdbc.usecase2.employee;

/**
 * Object representation of Ionic-protected fields of employee record.  These are held in database table
 * "employeeionic".
 * <p>
 * This object models a use case where an existing database table is constrained from adding additional columns to
 * accommodate Ionic metadata.
 */
public class EmployeeIonicFields {

    /**
     * Database id.  Value of zero indicates an employee that is not bound to the database table.
     */
    private final int id;

    /**
     * The Ionic-protected representation of the personal identifier for the employee.
     */
    private final String personalIdentifier;  // DB limited to 9 characters (normalized)

    /**
     * The Ionic-protected representation of the salary for the employee.
     */
    private final String salary;

    /**
     * Constructor.
     *
     * @param id                 the database id
     * @param personalIdentifier the employee personal identifier (Ionic-protected representation)
     * @param salary             the employee salary (Ionic-protected representation)
     */
    public EmployeeIonicFields(int id, String personalIdentifier, String salary) {
        this.id = id;
        this.personalIdentifier = personalIdentifier;
        this.salary = salary;
    }

    /**
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the employee personal identifier (Ionic-protected)
     */
    public String getPersonalIdentifier() {
        return personalIdentifier;
    }

    /**
     * @return the employee salary (Ionic-protected)
     */
    public String getSalary() {
        return salary;
    }
}
