package Observers;

import WeatherDataManagement.WeatherSensor;

import java.util.Map;

public interface Observer extends Comparable<Observer> {

    void update(Map<String, WeatherSensor> sensorMap);
}
