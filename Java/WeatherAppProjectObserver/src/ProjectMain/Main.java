package ProjectMain;

import Application.App;
import Observable.Csi;
import Observers.DataAppProcessor;
import WeatherDataManagement.WeatherSensorCreator;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) {
        WeatherSensorCreator weatherSensorCreator = new WeatherSensorCreator();
        Csi csi = new Csi(weatherSensorCreator.createSensorMap(Csi.Localisation.getLocalisationsList()), true, new TreeSet<>(),100);
        App app = new App(new HashMap<>(), new HashMap<>());
        DataAppProcessor dataAppProcessor = new DataAppProcessor(csi, app);
        csi.start();
        app.start();
        csi.setDataCollect(new AtomicBoolean(false));
    }
}
