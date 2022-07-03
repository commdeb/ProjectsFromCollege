package Application;

import org.junit.*;
import org.junit.Test;


import java.io.*;


import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserInterfaceTest {

    private final PrintStream standardOut = System.out;
    private final InputStream standartIn = System.in;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    App kupaMock = mock(App.class);
    User userMock = mock(User.class);
    UserInterface tester;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        tester = new UserInterface(kupaMock, new Scanner(System.in));
    }

    @Test
    public void printListTest1() {
        UserInterface tester = new UserInterface(kupaMock, new Scanner(System.in));
        List<Integer> integerList = List.of(1, 2, 3, 4);
        tester.printList(integerList, false);
        String expected = """
                1
                2
                3
                4""";
        assertEquals(expected, outputStreamCaptor.toString().trim());
        //osobiście nie wiem dlaczego to nie działa :(

    }

    @Test
    public void printListTest2() {
        UserInterface tester = new UserInterface(kupaMock, new Scanner(System.in));
        List<Integer> integerList = List.of(1, 2, 3, 4);
        tester.printList(integerList, true);
        String expected = """
                {1} 1
                {2} 2
                {3} 3
                {4} 4""";
        assertEquals(expected, outputStreamCaptor.toString().trim());

    }


    @Test
    public void unitPrinterTemperature() {
        assertEquals("Result:", "°C", tester.unitPrinter("temperature"));
    }

    @Test
    public void unitPrinterHumidity() {
        assertEquals("Result:", "%", tester.unitPrinter("humidity"));
    }

    @Test
    public void unitPrinterPressure() {
        assertEquals("Result:", "hPa", tester.unitPrinter("pressure"));
    }


    @After
    public void tearDown() {
        System.setOut(standardOut);
        System.setIn(standartIn);
    }
}