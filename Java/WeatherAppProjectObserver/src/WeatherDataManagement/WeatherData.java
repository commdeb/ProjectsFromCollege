package WeatherDataManagement;

import com.google.gson.annotations.SerializedName;

public class WeatherData  implements Comparable<WeatherData> {
    private final String localisation;
    @SerializedName("time of measurement")
    private final String collectionTime;
    private final WeatherSensor.Temperature temperature; // °C
    private final WeatherSensor.Humidity humidity; // %
    private final WeatherSensor.Pressure pressure; // hPa


    public WeatherData(String localisation,
                       String collectionTime,
                       WeatherSensor.Temperature temperature,
                       WeatherSensor.Humidity humidity,
                       WeatherSensor.Pressure pressure
    ) {
        this.localisation = localisation;
        this.collectionTime = collectionTime;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }


    public String getLocalisation() {
        return localisation;
    }

    public String getCollectionTime() {
        return collectionTime;
    }


    public double genericGetter(String name) {
        double number = 0;
        try {
            if (name.equals("humidity")) number = humidity.value;
            if (name.equals("temperature")) number = temperature.value;
            if (name.equals("pressure")) number = pressure.value;
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return number;
    }


    @Override
    public String toString() {
        StringBuilder sbuild = new StringBuilder();
        sbuild.append("localisation: ").append(localisation).append("\n").append("time of measurment: ");

        sbuild.append(collectionTime).append("\n").append("temperature: ");
        if (this.temperature != null) sbuild.append(String.format("%.1f", temperature.value)).append("°C");
        else sbuild.append("UNAVAILABLE");

        sbuild.append("\n").append("humidity: ");
        if (this.humidity != null) sbuild.append(String.format("%.0f", (humidity.value * 100))).append("%");
        else sbuild.append("UNAVAILABLE");

        sbuild.append("\n").append("pressure: ");
        if (this.pressure != null) sbuild.append(pressure.value).append("hPA");
        else sbuild.append("UNAVAILABLE");
        sbuild.append("\n");
        return sbuild.toString();
    }

    @Override
    public int compareTo(WeatherData ws) {
        int a = localisation.compareTo(ws.localisation);
        if (a == 0) a = collectionTime.compareTo(ws.collectionTime);
        return a;
    }
}
