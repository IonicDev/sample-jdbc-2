# Ionic Java SDK Sample Application / JDBC / Existing Database

The [Ionic SDK](https://dev.ionic.com/) provides an easy-to-use interface to the
[Ionic Platform](https://www.ionic.com/). In particular, the Ionic SDK exposes functions to perform Key Management
and Data Encryption.

The Ionic SDK allows users to apply encryption to both unstructured data (individual files) and to structured data held 
in relational databases. The use case for structured data might involve the encryption of some fields in one or 
more database tables.  

A [previous SDK sample](https://github.com/IonicDev/sample-jdbc-1) walked through a use case where certain fields in a 
newly created 
database were being Ionic-protected.  This sample was made simpler by the relaxed requirements around the structure of
the database table.  The Ionic-protected table fields were textual, and were of sufficient size to accommodate the 
Ionic ciphertext.

But there are cases where database schema changes are restricted.  In the case of space-constrained data fields, there 
may be no option to expand the field, or to convert its data type.  Non-textual column types (such as INTEGER or 
TIMESTAMP) are of a fixed size.  And the values of some database fields may have restrictions on their values, such as 
format, size, or allowed character set.
  
- phone numbers might be constrained to be exactly 10 digits
- social security numbers might be constrained to be exactly 9 digits

Any ciphertext that does not conform to these restrictions might be rejected by any element of the data stack.  An 
alternative strategy is needed to provide for these use cases.

This sample application attempts to capture some design patterns involved in applying Ionic-protection to existing 
databases, where data to be protected falls in one of these classes of data.  It uses the same toolset as the 
[previous SDK sample](https://github.com/IonicDev/sample-jdbc-1), and is presented as a test class containing a series 
of test cases.

- The *Apache DbUtils* library is used to wrap read access to the database.
- The *PostgreSQL* database management system application provides the back end store for the test data.
- The *JUnit* library is used within the *Apache Maven* Java software project management tool as the framework for 
running the test case.

## Prerequisites

- physical machine (or virtual machine) with the following software installed
  - [Git](https://git-scm.com/) distributed version control system 
  - Java Runtime Environment 7+ (either
    [OpenJDK JRE](https://openjdk.java.net/install/index.html) or
    [Oracle JRE](https://www.oracle.com/technetwork/java/javase/downloads/index.html))
  - [Apache Maven](https://maven.apache.org/) (Java software project management tool)
- a valid [Ionic Secure Enrollment Profile](https://dev.ionic.com/getting-started/create-ionic-profile) (a plaintext
json file containing access token data)

During the walk-through of this sample application, you will download the following:
- the PostgreSQL binary
- the git repository associated with this sample application

## Ionic Encryption

Ionic protection of a given data value produces a ciphertext corresponding to the original data.  There is additional 
information about each protected value that is produced by the protection operation.  This extra data is needed in 
order to successfully decrypt the data.

| Number of Bytes | Description |
| --- | --- |
| 11 | the Ionic key ID of the cryptography key used |
| 16 | the cryptography initialization vector (IV) |

One structured data use case would involve the encryption of each sensitive data cell with a unique key.  This 
usage would allow for very fine-grained control (via Ionic policy) of the data which may later be viewed by a client, 
but imposes an additional storage requirement for the protection of each sensitive value.

The Ionic SDK includes a set of chunk ciphers, which convert plaintext (in the form of strings or byte arrays) into a 
corresponding ciphertext.  The version 2 format *ChunkCipherV2* is the default SDK chunk cipher format.

| Plaintext | Chunk V2 Ciphertext |
| --- | --- |
| Hello, Ionic! | ~!2!D7GH6KPkyJN!g80RprW9WwzGmhE6s8A9hNxLHOQtlgTLvDYzya8! |

This format includes a version identifier token, a key identifier, initialization vector (IV) bytes, and the 
[base 64 encoding](https://en.wikipedia.org/wiki/Base64) of the encrypted plaintext (for compatibility with common 
8-bit character encodings).  Each of these components is needed to securely encrypt the data in a way that allows for 
recovery of the original data.  While necessary, they increase the storage space requirements for a given plaintext.

## Architecture

For this structured data use case, we will model a single table containing several data fields associated with an 
employee record.

#### Database Table [**Employee**]
| id | firstname | lastname | personalidentifier | salary | country
| --- | --- | --- | --- | --- | --- |
| 1 | Joseph | Martinez | 123456789 | 0 | US

We will apply Ionic protection to two of these fields: *personalidentifier* and *salary*.  The field 
*personalidentifier* will serve as a stand in for an id number like a social security number.  This field will be 
defined as containing exactly nine digits.  (For simplicity, we'll filter out the embedded dashes.)  The salary field
will be defined as an integer.   

For the purposes of this sample, we've locked the schema for the *Employee* table.  Since we are unable to modify the 
width or type of the existing fields, or to add new fields to the table, a new table will be added to hold these 
values.  The two tables will be bound together by means of a foreign key constraint.  The integer field *salary* will 
be protected by first being converted to its byte array representation.

While the key ID, IV, and ciphertext could be stored as separate fields in this new table for each protected field, 
this application will use ChunkCipherV2 to conglomerate all protection data for all fields into a single text 
field. The columns in the new table will have enough space to hold the chunk cipher representation of the fields.  This 
will simplify storage of the protected data.

#### Database Table [**EmployeeIonic**]
| fid | personalidentifier | salary |
| --- | --- | --- |
| 1 | ~!2!D7GH9gaMafA!+DGr9aRaGVSqXwQK506deb0ZulJc94svVQ! | ~!2!D7GH9oaOaVk!yk3D8Ve534QNIV2FOIp+a3W5xds! |

Since protected values are no longer being stored in the original table, what should be stored in them?  This sample 
use case sidesteps this problem by specifying a "default value" for each data field that is considered valid by all 
consumers of the data field.

- store the data in one place (no need for consistency check)
- handle textual and integral data types
- handle arbitrarily formatted data (values restricted to matching a certain pattern, like a social security number)

This design introduces some data constraints on an implementation which Ionic-protects data in this way:

- a table containing Ionic-protected fields must define a field (or set of fields) to be a table PRIMARY KEY,
- the corresponding Ionic table will contain a field corresponding to the original PRIMARY KEY, with a FOREIGN KEY 
constraint  
- each field to be protected in the original table will map 1-to-1 with a field in the Ionic table
- each Ionic table field will be textual, and must allocate sufficient space to accommodate the ciphertext of the 
largest possible field value.

## Sample Application Content

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/employee/Employee.java]**

This is a simple data object that holds the fields of a record in the database table *Employee*.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/employee/EmployeeIonicFields.java]**

This is a simple data object that holds the fields of a record in the database table *EmployeeIonic*.  The strings 
will hold the chunk cipher representation of the field value.  The id maps to the PRIMARY KEY in the table *Employee*. 

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/employee/EmployeeIonic.java]**

This object extends *Employee*, and is a means of joining the information in the tables *Employee* and *EmployeeIonic*
into a single object.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/employee/EmployeeIonicUtil.java]**

This utility class provides conversions between *Employee* and *EmployeeIonic*.  It uses the Ionic SDK class *Agent* to
handle the encrypt and decrypt functions, encapsulating the Ionic manipulation of the record data.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/employee/EmployeeUtil.java]**

This utility class provides a means of synthesizing new test Employee records.  The sensitive field values are 
generated in a way that allows for their out-of-band recovery, to verify the cryptography operations.  

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/jdbc/EmployeeIonicJdbcUtil.java]**

This class loads the content of the *EmployeeIonic* table into memory, for efficiency in the context of testing.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/jdbc/IonicTypes.java]**

Ionic encryption operates on strings and byte arrays.  For this test, the sensitive integer value is converted into its
byte array representation during the encryption operation.  This operation is reversed on decryption.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/jdbc/JdbcUtil.java]**

This utility class uses the test configuration to establish a JDBC connection to the test database. 

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/jdbc/RowSet.java]**

This Java class contains a very simple object implementation that can be used to hold the content of a database 
ResultSet.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/jdbc/SampleResultSetHandler.java]**

This Java class implements the Apache DbUtils interface ResultSetHandler. When an SQL SELECT is called, the data is 
returned in one or more ResultSet objects.

**[javasdk-sample2-jdbc/src/test/java/com/ionic/sdk/addon/jdbc/usecase2/test/CreateReadJdbcTest.java]**

The first test case opens a connection to the database, then crafts new records and inserts them into the sample 
database tables.  The test configuration specifies database parameters.  The Ionic class ChunkCipherV2 will be used to 
encrypt record fields as needed.  The data to be used is randomized from lists of common names.

The second test case queries the content of the database tables *Employee* and *EmployeeIonic*.  The table id fields 
are used to correlate *Employee* records to the corresponding sensitive data in the *EmployeeIonic* table.  Any data 
decryption errors are written to the console.

The third test case writes the raw content of the two tables to the console.  This illustrates the protection of the 
data at rest in the database.

**[javasdk-sample2-jdbc/src/test/resources/ionic/ionic.sep.plaintext.json]**

The template JSON in this file should be replaced with the text of the Ionic Secure Enrollment Profile to be used. The 
SEP contains configuration specifying the Ionic server to use, as well as data to identify the client making the 
requests.

**[javasdk-sample2-jdbc/src/test/resources/jdbc/sql.sample.setup.txt]**

These SQL commands will be used to create the tables used by the sample.  The columns in table *EmployeeIonic* are 
defined to be of sufficient length to hold the chunk cipher representation of the sensitive values in table *Employee*.

**[javasdk-sample2-jdbc/src/test/resources/ionic/logging.properties]**

This file specifies the configuration of Java's logging facility.

**[javasdk-sample2-jdbc/src/test/resources/ionic/test.properties.xml]**

This file contains configuration settings for the sample.

## Sample Application Walk-through

1. Download [PostgreSQL image](https://www.enterprisedb.com/download-postgresql-binaries).  (This walk-through was
prepared using version 10.8 of the Postgres software.)

1. Inflate image into an empty folder on your filesystem.

1. Open a *Command Prompt* window.  Navigate to the root folder of the unzipped PostgreSQL instance.
   1. Run the following command to create the sample database.  (You will be prompted twice to enter/reenter a
        superuser password for the database.)
        ```shell
        bin\initdb.exe -D data -U postgres -W -E UTF8 -A scram-sha-256
        ```

        ```shell
        postgresql\pgsql>bin\initdb.exe -D data -U postgres -W -E UTF8 -A scram-sha-256
        The files belonging to this database system will be owned by user "demouser".
        This user must also own the server process.

        The database cluster will be initialized with locale "English_United States.1252".
        The default text search configuration will be set to "english".

        Data page checksums are disabled.

        Enter new superuser password:
        Enter it again:

        creating directory data ... ok
        creating subdirectories ... ok
        selecting default max_connections ... 100
        selecting default shared_buffers ... 128MB
        selecting dynamic shared memory implementation ... windows
        creating configuration files ... ok
        running bootstrap script ... ok
        performing post-bootstrap initialization ... ok
        syncing data to disk ... ok

        Success. You can now start the database server using:

            bin/pg_ctl -D data -l logfile start


        postgresql\pgsql>
        ```
   1. Run the following command to start the PostgreSQL server.
        ```shell
        bin\pg_ctl.exe -D data start
        ```
        ```shell
        postgresql\pgsql>bin\pg_ctl.exe -D data start
        waiting for server to start....
        listening on IPv4 address "127.0.0.1", port 5432
        listening on IPv6 address "::1", port 5432
        ...
        database system is ready to accept connections
         done
        server started

        postgresql\pgsql>
        ```

1. Open a second *Command Prompt* window.  Navigate to the root folder of the unzipped PostgreSQL instance.
   1. Run the following command to enter the PSQL command console.
        ```shell
        bin\psql.exe -U postgres
        ```
        ```shell
        postgresql\pgsql>bin\psql.exe -U postgres
        Password for user postgres:
        psql (10.8)
        WARNING: Console code page (437) differs from Windows code page (1252)
                 8-bit characters might not work correctly. See psql reference
                 page "Notes for Windows users" for details.
        Type "help" for help.

        postgres=#
        ```

   1. Check the PostgreSQL version (this also verifies connectivity to the server process).
        ```shell
        postgres=# SELECT version();
                             version
        ------------------------------------------------------------
        PostgreSQL 10.8, compiled by Visual C++ build 1800, 64-bit
        (1 row)

        postgres=#
        ```

   1. Create the sample tables in the database by performing a copy/paste operation from the sample resource
   *sql.sample.setup.txt* into the Command Prompt window.  Create each table in a separate PSQL operation.
        ```shell
        postgres=# CREATE TABLE employee(
        postgres=# id serial PRIMARY KEY,
        postgres=# firstname VARCHAR (24),
        postgres=# lastname VARCHAR (24),
        postgres=# personalidentifier VARCHAR (9),
        postgres=# salary INTEGER,
        postgres=# country VARCHAR (3));
        CREATE TABLE
        postgres=#
        ```
        ```shell
        postgres=# CREATE TABLE employeeionic(
        postgres=# fid INTEGER REFERENCES employee(id),
        postgres=# personalidentifier VARCHAR(51),
        postgres=# salary VARCHAR(44));
        CREATE TABLE
        postgres=#
        ```

1. Open a third *Command Prompt* window.  Clone the git sample application repository into an empty folder on your
filesystem.
    ```shell
    git clone https://github.com/IonicDev/sample-jdbc-2.git
    ```

1. Substitute the JSON text of your Ionic Secure Enrollment Profile into the file
    **[sample-jdbc-2/src/test/resources/ionic/ionic.sep.plaintext.json]**.

1. Navigate to the root folder of the *sample-jdbc-2* repository. Run the following command to package the
sample application (which will also execute the sample's unit test):
    ```shell
    mvn clean package
    ```
    ```shell
    sample-jdbc-2>mvn clean package
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Ionic Java SDK Sample Application #2, JDBC usage 0.0.0-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    ...
    [INFO] -------------------------------------------------------
    [INFO]  T E S T S
    [INFO] -------------------------------------------------------
    [INFO] Running com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest testJdbc_2_ReadRecords PERSONAL IDENTIFIER RECOVERED FOR RECORD 1
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest testJdbc_2_ReadRecords SALARY RECOVERED FOR RECORD 1
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest testJdbc_2_ReadRecords PERSONAL IDENTIFIER RECOVERED FOR RECORD 2
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest testJdbc_2_ReadRecords SALARY RECOVERED FOR RECORD 2
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest testJdbc_3_OutputRecords SHOW TABLE EMPLOYEE
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest readRecords 1 William Johnson 000000000 0 US
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest readRecords 2 Jennifer Jones 000000000 0 UK
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest testJdbc_3_OutputRecords SHOW TABLE EMPLOYEEIONIC
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest readRecords 1 ~!2!D7GH9eyGK2o!gMx3fVL5bthcot4MZ+R75vLF0dBMkUzKSg! ~!2!D7GHDw41PKQ!xFKk5hVVN9uJiKXjYl7CM2A7ExY!
    INFO com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest readRecords 2 ~!2!D7GH9WyEK8M!jQsNSImNNlYGReEo6qOdPJkEcw5b5uBb3Q! ~!2!D7GHD443PA0!hLnJRciZsTkokHIBPQALtc9lfuU!
    [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.2 s - in com.ionic.sdk.addon.jdbc.usecase2.test.CreateReadJdbcTest
    ...
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 6.870 s
    [INFO] Finished at: 2019-06-21T11:08:04-04:00
    [INFO] Final Memory: 17M/215M
    [INFO] ------------------------------------------------------------------------
    
    sample-jdbc-2>
    ```

The first test created two new Employee records, and inserted their values into database tables *Employee* and 
*EmployeeIonic*.  The second test fetched this data, and checked the protected values against their expected 
value.  (Had any decrypted value differed from expectations, this test would fail.)  The third test wrote the database 
table content out to the console.  In this test, you can see the default data in the sensitive fields of table 
*Employee*.

## Conclusion
As with the [previous SDK sample](https://github.com/IonicDev/sample-jdbc-1), it is a straightforward task to 
integrate Ionic data
protection capabilities into database applications.  While it can be simpler to design Ionic into new applications /
database schemas, the adaptation of existing data is also possible.  Ionic protection of a data value involves the
storage of extra metadata; there are several ways this data may be persisted.  For data consistency reasons, it is
preferable that the Ionic metadata be located as close as possible to the data it protects.

Ionic's platform is powerful and flexible enough to be broadly applicable to the data protection needs of modern
organizations.
