package ca.uwaterloo.usmmonitor;

import android.provider.BaseColumns;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ca.uwaterloo.usmmonitor.database.ProcessInfoContract;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by liuyangren on 2017-09-01.
 */

public class ContractClassUnitTest {

    @Test
    public void inner_class_exists() throws Exception {
        Class[] innerClasses = ProcessInfoContract.class.getDeclaredClasses();
        assertEquals("There should be 1 Inner class inside the contract class", 1, innerClasses.length);
    }

    @Test
    public void inner_class_type_correct() throws Exception{
        Class[] innerClasses = ProcessInfoContract.class.getDeclaredClasses();
        assertEquals("Cannot find inner class to complete unit test", 1, innerClasses.length);
        Class entryClass = innerClasses[0];
        assertTrue("Inner class should implement the BaseColumns interface", BaseColumns.class.isAssignableFrom(entryClass));
        assertTrue("Inner class should be final", Modifier.isFinal(entryClass.getModifiers()));
        assertTrue("Inner class should be static", Modifier.isStatic(entryClass.getModifiers()));

    }

    @Test
    public void inner_class_members_correct() throws Exception{
        Class[] innerClassees = ProcessInfoContract.class.getDeclaredClasses();
        assertEquals("Cannot fine inner class to complete unit test", 1, innerClassees.length);
        Class entryClass = innerClassees[0];
        Field[] alllFields = entryClass.getDeclaredFields();
        assertEquals("There should be exactly 7 String members in the inner class", 1+6, alllFields.length);
        for (Field field : alllFields) {
            assertTrue("All members in the contract class should be Strings", field.getType() == String.class);
            assertTrue("All members in the contract class should be final", Modifier.isFinal(field.getModifiers()));
            assertTrue("All members in the contract class should be stsatic", Modifier.isStatic(field.getModifiers()));

        }

    }
}
