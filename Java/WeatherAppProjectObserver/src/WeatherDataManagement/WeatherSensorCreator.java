package WeatherDataManagement;


import java.util.HashMap;

import java.util.List;
import java.util.Map;

public class WeatherSensorCreator {

    public  Map<String, WeatherSensor> createSensorMap(Map<String, List<String>> localisationMap) {
        Map<String, WeatherSensor> weatherSensors = new HashMap<>();

        for (String loc : localisationMap.keySet()) {
            WeatherSensor weatherSensor = new WeatherSensor(loc);
            for (String measurement : localisationMap.get(loc)) {
                if (measurement.equals("temperature"))
                    weatherSensor.setTemperature(new WeatherSensor.Temperature());
                if (measurement.equals("humidity"))
                    weatherSensor.setHumidity(new WeatherSensor.Humidity());
                if (measurement.equals("pressure"))
                    weatherSensor.setPressure(new WeatherSensor.Pressure());


            }
            weatherSensors.put(loc, weatherSensor);
        }

        return weatherSensors;
    }

}
