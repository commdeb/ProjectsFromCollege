package WeatherDataManagement;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class WeatherDataTest {

    WeatherData tester;

    @Before
    public void setUp() throws Exception {
        tester = new WeatherData(null, null, null, null, null);

    }

    @Test
    public void genericGetterWrongVariableNameTest() {
        assertEquals(0, tester.genericGetter("wrondVariableName"), 0.0);
    }

    @Test
    public void genericGetterNullPointerExceptionName1Test() {
        try {
            tester.genericGetter("humidity");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void genericGetterNullPointerExceptionName2Test() {
        try {
            tester.genericGetter("temperature");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void genericGetterNullPointerExceptionName3Test() {
        try {
            tester.genericGetter("pressure");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void genericGetterPressureTest() {
        tester = new WeatherData(null, null, null, null, new WeatherSensor.Pressure(1000));
        assertEquals(1000, tester.genericGetter("pressure"), 0.0);
    }

    @Test
    public void genericGetterTemperatureTest() {
        tester = new WeatherData(null, null, new WeatherSensor.Temperature((float)0.2), null, null);
        assertEquals(0.2, tester.genericGetter("temperature"), 0.0);
    }

    @Test
    public void genericGetterHumidityTest() {
        tester = new WeatherData(null, null, null, new WeatherSensor.Humidity((float)0.2), null);
        assertEquals(0.2, tester.genericGetter("humidity"), 0.0);
    }

}
