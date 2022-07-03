package WeatherDataManagement;

import java.time.LocalTime;

import java.util.Random;

public class WeatherSensor {

    private final String localisation;
    private String collectionTime;
    private Temperature temperature; // °C
    private Humidity humidity; // %
    private Pressure pressure; // hPa

    public WeatherSensor(String localisation) {
        this.localisation = localisation;

    }

    public void getData() {
Random random = new Random();
        this.collectionTime = String.format("%tT", LocalTime.now());
        if (temperature != null)
            this.temperature.value = (double) (random.nextInt(200 - (-200)) + (-200)) / 10;
        if (humidity != null)
            this.humidity.value = (double) (random.nextInt(5 - 2) + 2) / 10;
        if (pressure != null)
            this.pressure.value = random.nextInt(1100 - 1000) + 1000;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public Pressure getPressure() {
        return pressure;
    }

    public WeatherData makeDataCopy() {
        Temperature temperature = null;
        Humidity humidity = null;
        Pressure pressure = null;
        if (this.temperature != null) temperature = new Temperature(this.temperature.value);
        if (this.humidity != null) humidity = new Humidity(this.humidity.value);
        if (this.pressure != null) pressure = new Pressure(this.pressure.value);

        return new WeatherData(localisation, collectionTime, temperature, humidity, pressure);

    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(Humidity humidity) {
        this.humidity = humidity;
    }

    public void setPressure(Pressure pressure) {
        this.pressure = pressure;
    }

    protected static class Temperature {
        double value;

        public Temperature(double value) {
            this.value = value;
        }

        public Temperature() {
        }

    }

    protected static class Humidity {
        double value;

        public Humidity(double value) {
            this.value = value;
        }

        public Humidity() {
        }

    }

    protected static class Pressure {
        int value;

        public Pressure(int value) {
            this.value = value;
        }

        public Pressure() {
        }


    }
}


