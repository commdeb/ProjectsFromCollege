package Application;

import WeatherDataManagement.WeatherData;

import java.util.*;

public class App {
    private User user;
    private final Map<String, List<WeatherData>> archivedMeasures;
    private final Map<String, WeatherData> latestMeasures;
    private UserInterface userInterface;


    public App(Map<String, List<WeatherData>> archivedMeasures, Map<String, WeatherData> latestMeasures) {
        this.archivedMeasures = archivedMeasures;
        this.latestMeasures = latestMeasures;

    }


    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }


    public void setUserInterface(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public Map<String, List<WeatherData>> getArchivedMeasures() {
        return archivedMeasures;
    }


    public Map<String, WeatherData> getLatestMeasures() {
        return latestMeasures;
    }


    public void setNewData(WeatherData weatherData) {
        latestMeasures.put(weatherData.getLocalisation(), weatherData);
        archivedMeasures.get(weatherData.getLocalisation()).add(weatherData);

    }

    protected void subscribe(String loc) {
        user.getSubedLocalisations().add(loc);
        archivedMeasures.put(loc, new ArrayList<>());
    }

    protected void unSubscribe(String loc) {
        latestMeasures.remove(loc);
        archivedMeasures.remove(loc);
        user.getSubedLocalisations().remove(loc);
    }


    protected List<WeatherData> getMaxMin(String loc, String variable, int version, Map<String, List<String>> localisatonMap) {
        try {
            if (localisatonMap.get(loc).contains(variable) && (version == 1 || version == -1)) {
                List<WeatherData> weatherData = new ArrayList<>(archivedMeasures.get(loc));
                List<WeatherData> weatherDataCopied = new ArrayList<>();

                weatherData.sort(new Comparator<>() {
                    @Override
                    public int compare(WeatherData o1, WeatherData o2) {
                        return Double.compare(o1.genericGetter(variable), o2.genericGetter(variable)) * (version * (-1));
                    }
                });

                int i = 0;
                while (weatherData.get(i).genericGetter(variable) == weatherData.get(0).genericGetter(variable)) {
                    weatherDataCopied.add(weatherData.get(i));
                    if (++i == weatherData.size()) break;
                }
                return weatherDataCopied;
            } else return null;
        } catch (NullPointerException e) {
            System.out.println("Error: Wrong localisation");
            e.printStackTrace();
        }
        return null;
    }

    protected double calculateAverage(String loc, String variable, Map<String, List<String>> localisatonMap) {
        double average = getUser().hashCode();
        try {
            if (localisatonMap.get(loc).contains(variable)) {
                average = 0;
                List<WeatherData> weatherData = new ArrayList<>(archivedMeasures.get(loc));
                for (WeatherData weatherD : weatherData) {
                    average += weatherD.genericGetter(variable);
                }
                average /= weatherData.size();
            }
        } catch (NullPointerException e) {
            System.out.println("Error: Wrong localization!");
            e.printStackTrace();
        }
        return average;
    }


    public void start() {
        setUserInterface(new UserInterface(this, new Scanner(System.in)));
        userInterface.open();
        System.out.println("Good bye ;)");
    }


}


