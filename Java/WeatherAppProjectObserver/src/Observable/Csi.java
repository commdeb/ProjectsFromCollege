package Observable;

import Observers.Observer;
import WeatherDataManagement.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Csi extends Thread implements Subject {
    private final Set<Observer> observerSet;
    private final Map<String, WeatherSensor> sensorMap;
    private AtomicBoolean dataCollect;
    private int sleepMilliseconds;


    public Csi(Map<String, WeatherSensor> sensorMap, boolean dataCollect, Set<Observer> observers, int sleepMilliseconds) {
        this.sensorMap = sensorMap;
        this.dataCollect = new AtomicBoolean(dataCollect);
        this.observerSet = observers;
        this.sleepMilliseconds = sleepMilliseconds;
    }

    @Override
    public void run() {
        while (dataCollect.get()) {
            collectFromSensors();
            if (!observerSet.isEmpty()) notifyObservers();

            try {
                Thread.sleep(sleepMilliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void collectFromSensors() {
        for (String str : sensorMap.keySet()) {
            sensorMap.get(str).getData();
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        observerSet.add(observer);

    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observerSet) {
            observer.update(sensorMap);
        }
    }

    public Set<Observer> getObserverSet() {
        return observerSet;
    }

    @Override
    public void removeObserver(Observer observer) {
        observerSet.remove(observer);
    }

    public void setDataCollect(AtomicBoolean dataCollect) {
        this.dataCollect = dataCollect;
    }

    public static class Localisation {
        private static Map<String, List<String>> localisationsList =
                Map.of("Wroclaw", List.of("humidity", "pressure"),
                        "Walbrzych", List.of("temperature", "pressure"),
                        "Legnica", List.of("temperature", "humidity"),
                        "JeleniaGora", List.of("temperature", "humidity", "pressure"),
                        "Lubin", List.of("temperature", "humidity", "pressure"),
                        "Glogow", List.of("temperature", "humidity", "pressure"),
                        "Swidnica", List.of("humidity", "pressure"),
                        "Boleslawiec", List.of("temperature", "humidity", "pressure"),
                        "Olesnica", List.of("temperature", "humidity", "pressure"),
                        "Olawa", List.of("temperature", "humidity", "pressure")
                );


        public static Map<String, List<String>> getLocalisationsList() {
            return localisationsList;
        }
    }
}
