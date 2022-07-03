package Application;

import org.junit.Test;
import WeatherDataManagement.WeatherData;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class AppTest {
    private App tester;


    @Before
    public void setUp() throws Exception {
        tester = new App(new HashMap<>(), new HashMap<>());
        tester.setUser(new User("sth", new LinkedList<>()));
    }

    @Test
    public void setNewDataTest() {

        WeatherData weatherDataMock = mock(WeatherData.class);
        when(weatherDataMock.getLocalisation()).thenReturn("default");
        tester.getArchivedMeasures().put("default", new ArrayList<>());
        tester.setNewData(weatherDataMock);
        assertEquals("Result", weatherDataMock, tester.getLatestMeasures().get("default"));
        assertEquals("Result", weatherDataMock, tester.getArchivedMeasures().get("default").get(0));

    }

    @Test
    public void subscribeTest() {

        tester.subscribe("default");
        assertTrue("Result", tester.getUser().getSubedLocalisations().contains("default"));
        assertTrue("Result", tester.getArchivedMeasures().get("default") instanceof ArrayList);
    }

    @Test
    public void unSubscribeTest() {
        List<WeatherData> weatherDataListStub = new ArrayList<>();
        WeatherData weatherDataMock = mock(WeatherData.class);
        tester.getUser().getSubedLocalisations().add("default");
        tester.getLatestMeasures().put("default", weatherDataMock);
        tester.getArchivedMeasures().put("default", weatherDataListStub);
        tester.unSubscribe("default");
        assertTrue("Result", tester.getUser().getSubedLocalisations().isEmpty() &&
                tester.getLatestMeasures().isEmpty() && tester.getArchivedMeasures().isEmpty());

    }

    @Test
    public void getMaxMinWrongLocalisationNameTest() {
        Map<String, List<String>> localisationMapStub = Map.of("notsth", List.of("notsth"));
        try {
            tester.getMaxMin("sth", "sth", 1, localisationMapStub);
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getMaxMinWrongVariableNameTest() {
        Map<String, List<String>> localisationMapStub = Map.of("sth", List.of("notsth"));
        assertNull(tester.getMaxMin("sth", "sth", 1, localisationMapStub));

    }

    @Test
    public void getMaxMinWrongVersionNumberTest() {
        Map<String, List<String>> localisationMapStub = Map.of("sth", List.of("sth"));
        assertNull(tester.getMaxMin("sth", "sth", 2, localisationMapStub));

    }


    @Test
    public void calculateAverageWrongLocalisationTest() {
        Map<String, List<String>> localisationMapStub = Map.of("notsth", List.of("notsth"));
        try {
            tester.calculateAverage("sth", "sth", localisationMapStub);
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void calculateAverageWrongVariableTest() {
        Map<String, List<String>> localisationMapStub = Map.of("sth", List.of("notsth"));
        assertEquals(tester.getUser().hashCode(), tester.calculateAverage("sth", "sth", localisationMapStub), 0.0);

    }
    

    @Test
    public void calculateAverage() {
        Map<String, List<String>> localisationMapStub = Map.of("sth", List.of("sth"));
        WeatherData[] weatherDataMocks = {mock(WeatherData.class), mock(WeatherData.class), mock(WeatherData.class)};
        Map<String, List<WeatherData>> archivedMapStub = new HashMap<>(Map.of("sth", Arrays.asList(weatherDataMocks)));
        when(weatherDataMocks[0].genericGetter("sth")).thenReturn((double) 1);
        when(weatherDataMocks[1].genericGetter("sth")).thenReturn((double) 1);
        when(weatherDataMocks[2].genericGetter("sth")).thenReturn((double) 1);
        tester = new App(archivedMapStub, new HashMap<>());
        tester.setUser(new User("sth", new LinkedList<>()));
        assertEquals(1, tester.calculateAverage("sth", "sth", localisationMapStub), 0.0);
    }

    @Test
    public void start() {
    }
}