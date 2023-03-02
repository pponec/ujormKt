package org.ujorm.kotlin.core;

//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.ujorm.kotlin.core.entity.*;
//import org.ujorm.kotlin.core.impl.AbstractEntityProvider;
//import org.ujorm.kotlin.core.entity.*;
//import java.time.LocalDate;

public class CoreTestForJavaLanguage {

//    private final MyDatabase myDatabase; // = new MyDatabase().close();
//
//    /** Registrer memtamodel and close it */
//    public CoreTestForJavaLanguage() {
//        myDatabase = new MyDatabase();
//        myDatabase.utils().addAll( myDatabase.employees, myDatabase.departments);
//        myDatabase.close();
//    }
//
//    @Test
//    void readAndWrite() throws ReflectiveOperationException {
//
//        // Create new intance:
//        var employees = myDatabase.employees;
//        var departments = myDatabase.departments;
//        var employee = employees.newObject();
//        employee.setId(11);
//        employee.setName("John");
//        employee.setHigherEducation(false);
//        employee.setContractDay( LocalDate.now());
//        employee.setDepartment(createDepartment(2, "D"));
//        employee.setSuperior(null);
//
//        // Write values:
//        int id = employee.get(employees.getId());
//        String name = employee.get(employees.getName());
//        boolean higherEducation = employee.get(employees.getHigherEducation());
//        LocalDate contractDay = employee.get(employees.getContractDay());
//        Department department = employee.get(employees.getDepartment());
//        Employee superior= employee.get(employees.getSuperior());
//
//        // Read values:
//        employee.set(employees.getId(), id);
//        employee.set(employees.getName(), name);
//        employee.set(employees.getHigherEducation(), higherEducation);
//        employee.set(employees.getContractDay(), contractDay);
//        employee.set(employees.getDepartment(), department);
//        employee.set(employees.getSuperior(), superior);
//
//        // Composed properties:
//        String departmentName = employee.get(employees.getDepartment().plus(departments.getName()));
//        employee.set(employees.getDepartment().plus(departments.getName()), departmentName);
//
//        Assertions.assertEquals(11, employee.getId());
//        Assertions.assertEquals("John", employee.getName());
//        Assertions.assertEquals(false, employee.getHigherEducation());
//        Assertions.assertEquals("D", employee.getDepartment().getName());
//
//        // Clone the employee:
//        var twin = myDatabase.utils().clone(employee);
//        Assertions.assertEquals(11, twin.getId());
//        Assertions.assertEquals("John", twin.getName());
//        Assertions.assertEquals(false, twin.getHigherEducation());
//        Assertions.assertEquals("D", twin.getDepartment().getName());
//    }
//
//    private Department createDepartment(int id, String name)
//            throws ReflectiveOperationException {
//        final var result = myDatabase.departments.newObject();
//        result.setId(id);
//        result.setName(name);
//        return result;
//    }
//
//    /** Local metamodel */
//    private static class MyDatabase extends AbstractEntityProvider {
//        public final Employees employees = new Employees();
//        public final Departments departments = new Departments();
//    }
}
