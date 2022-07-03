package Application;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void makeSaveAndClear() {
        String[] localisation = {"loc1", "loc2", "loc3"};
        List<String> subsTester = new LinkedList<>(Arrays.asList(localisation));
        User tester = new User("sth", subsTester);
        tester.makeSaveAndClear(subsTester);
        assertTrue(tester.getSubedLocalisations().isEmpty());
        assertTrue(tester.getSavedLocalisations().contains("loc1") &&
                tester.getSavedLocalisations().contains("loc2") &&
                tester.getSavedLocalisations().contains("loc3")
        );
    }
}