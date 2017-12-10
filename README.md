# SqlAssist Transform

Groovy AST Transformation to accept simple SQL DSL. Inspired by the simplicity of the Spock framework **where** block. Currently supporting inserts only.

## Getting Started

### Prerequisites

* Java
* Groovy

### Installation

#### Gradle

```
testCompile group:'info.akang', name:'groovy-sql-transform', version: '1.0'
```

#### Maven

```
<dependency>
    <groupId>info.akang</groupId>
    <artifactId>groovy-sql-transform</artifactId>
    <version>1.0</version>
</dependency>
```

* Deployed to Maven Central
* Tested with Groovy version *2.4.13* and Spock version *1.1-groovy-2.4*

### Usage

* Add @SqlAssist annotation at either class level or method level to support compile-time transformation.
* Example:

```groovy


@SqlAssist
class Test {

    def someMethod() {
        Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")
        
        sql.execute """
            CREATE TABLE test1
            (
                id INTEGER,
                some_date DATE,
                qty SMALLINT,
                price DECIMAL(6,2)
            );"""

        // insert 2 rows with 'sql' object into the 'test1' table
        // column header line is required and must be the name of the columns to be inserted into.
        insert test1 with sql
        id     | some_date      | qty   | price
        1      | '2017-01-05'   | 2     | 1.99
        2      | '2017-01-05'   | 5     | 3.99
    }
}
```

## Running the Tests

```
./gradlew test
```

* Tests include using annotation at class and method levels. Also tests within a Spock Specification.

