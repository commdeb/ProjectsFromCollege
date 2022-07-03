package Observers;

import Application.App;
import Observable.Subject;
import WeatherDataManagement.WeatherSensor;

import java.util.Map;

public class DataAppProcessor implements Observer {
    App app;
    Subject weatherInfo;

    public DataAppProcessor(Subject weatherInfo, App app) {
        this.app = app;
        this.weatherInfo = weatherInfo;
        this.weatherInfo.registerObserver(this);

    }


    @Override
    public void update(Map<String, WeatherSensor> sensorMap) {
        if (app.getUser() != null) {
            if (!app.getUser().getSubedLocalisations().isEmpty()) {
                for (String loc : app.getUser().getSubedLocalisations()) {
                    app.setNewData(sensorMap.get(loc).makeDataCopy());
                }
            }
        }
    }


    @Override
    public int compareTo(Observer ob) {
        return Integer.compare(hashCode(), ob.hashCode());
    }
}



