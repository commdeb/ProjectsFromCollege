package Observers;

import Application.App;
import Application.User;
import Observable.Csi;
import WeatherDataManagement.WeatherData;
import WeatherDataManagement.WeatherSensor;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataAppProcessorTest {
    App kupaStub;
    Csi csiStub;

    @Before
    public void setUp() throws Exception {
        kupaStub = new App(new HashMap<>(), new HashMap<>());
        csiStub = new Csi(new HashMap<>(), true, new TreeSet<>(), 5000);
    }

    @Test
    public void updateAppUserNullTest() {
        WeatherSensor weatherSensorMock1 = mock(WeatherSensor.class);
        Map<String, WeatherSensor> sensorMapStub = new HashMap<>();
        sensorMapStub.put("local1", weatherSensorMock1);
        kupaStub.setUser(null);
        DataAppProcessor tester = new DataAppProcessor(csiStub, kupaStub);
        tester.update(sensorMapStub);
        assertTrue(kupaStub.getArchivedMeasures().isEmpty() && kupaStub.getLatestMeasures().isEmpty());
    }

    @Test
    public void updateAppUserSubListEmptyTest() {
        WeatherSensor weatherSensorMock1 = mock(WeatherSensor.class);
        Map<String, WeatherSensor> sensorMapStub = new HashMap<>();
        sensorMapStub.put("local1", weatherSensorMock1);
        kupaStub.setUser(new User("username", new LinkedList<>()));
        DataAppProcessor tester = new DataAppProcessor(csiStub, kupaStub);
        tester.update(sensorMapStub);
        assertTrue(kupaStub.getArchivedMeasures().isEmpty() && kupaStub.getLatestMeasures().isEmpty());
    }

    @Test
    public void updateNormalConditionsTest() {
        WeatherSensor weatherSensorMock1 = mock(WeatherSensor.class);
        Map<String, WeatherSensor> sensorMapStub = new HashMap<>();
        sensorMapStub.put("local1", weatherSensorMock1);
        kupaStub.setUser(new User("username", new LinkedList<>(List.of("local1"))));
        DataAppProcessor tester = new DataAppProcessor(csiStub, kupaStub);
        WeatherData weatherDataMock = mock(WeatherData.class);
        when(sensorMapStub.get("local1").makeDataCopy()).thenReturn(weatherDataMock);
        when(weatherDataMock.getLocalisation()).thenReturn("local1");
        kupaStub.getArchivedMeasures().put("local1", new ArrayList<>());
        tester.update(sensorMapStub);

        assertEquals(weatherDataMock, kupaStub.getArchivedMeasures().get("local1").get(0));
        assertEquals(weatherDataMock, kupaStub.getLatestMeasures().get("local1"));
    }
}