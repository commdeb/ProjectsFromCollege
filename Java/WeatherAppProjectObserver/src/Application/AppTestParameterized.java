package Application;

import WeatherDataManagement.WeatherData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class AppTestParameterized {
    private App tester;
    private double value0;
    private double value1;
    private double value2;
    private int version;

    public AppTestParameterized(double value0, double value1, double value2, int version) {
        this.value0 = value0;
        this.value1 = value1;
        this.value2 = value2;
        this.version = version;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{{0, 1, 1, 1}, {1, 0, 0, -1}};
        return Arrays.asList(data);
    }

    @Test
    public void getMaxMinGetMaxTest() {
        Map<String, List<String>> localisationMapStub = Map.of("sth", List.of("sth"));
        WeatherData[] weatherDataMocks = {mock(WeatherData.class), mock(WeatherData.class), mock(WeatherData.class)};
        Map<String, List<WeatherData>> archivedMapStub = new HashMap<>(Map.of("sth", Arrays.asList(weatherDataMocks)));
        when(weatherDataMocks[0].genericGetter("sth")).thenReturn(value0);
        when(weatherDataMocks[1].genericGetter("sth")).thenReturn(value1);
        when(weatherDataMocks[2].genericGetter("sth")).thenReturn(value2);
        tester = new App(archivedMapStub, new HashMap<>());
        List<WeatherData> result = tester.getMaxMin("sth", "sth", version, localisationMapStub);
        assertTrue(result.contains(weatherDataMocks[1]) && result.contains(weatherDataMocks[2]));

    }

}
