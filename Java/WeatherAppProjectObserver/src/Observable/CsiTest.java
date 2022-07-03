package Observable;

import Observers.Observer;
import WeatherDataManagement.WeatherSensor;
import org.junit.Test;

import java.util.HashMap;
import java.util.TreeSet;

import org.junit.*;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import java.util.*;

import static org.mockito.Mockito.*;

public class CsiTest {
    Csi csi;

    @Before
    public void setUp() throws Exception {
        csi = new Csi(new HashMap<>(), true, new TreeSet<>(), 5000);
    }

    @Test(expected = Exception.class)
    public void collectFromSensorsTest() {
        WeatherSensor weatherSensorMock1 = mock(WeatherSensor.class);
        Map<String, WeatherSensor> sensorMapStub = new HashMap<>();
        sensorMapStub.put("local1", weatherSensorMock1);
        Csi csi = new Csi(sensorMapStub, true, new TreeSet<>(), 5000);
        csi.collectFromSensors();

        Mockito.doThrow(new Exception()).when(weatherSensorMock1).getData();


    }

    @Test
    public void registerObserverTest() {
        Observer observerMock = mock(Observer.class);
        csi.registerObserver(observerMock);
        assertTrue(csi.getObserverSet().contains(observerMock));
    }

    @Test(expected = Exception.class)
    public void notifyObserversTest() {
        Observer observerMock = mock(Observer.class);
        csi.registerObserver(observerMock);
        csi.notifyObservers();

        Mockito.doThrow(new Exception()).when(observerMock).update(null);

    }

    @Test
    public void removeObserverTest() {
        Observer observerMock = mock(Observer.class);
        csi.removeObserver(observerMock);
        assertFalse(csi.getObserverSet().contains(observerMock));
    }
}