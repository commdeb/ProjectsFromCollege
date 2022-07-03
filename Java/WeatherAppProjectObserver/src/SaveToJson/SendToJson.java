package SaveToJson;

import WeatherDataManagement.WeatherData;
import com.google.gson.Gson;

import java.io.*;
import java.util.List;
import java.util.Map;

public class SendToJson {

    public static boolean toJson(Map<String, List<WeatherData>> archivedMeasures, String path) {
        boolean isSent = false;

        Gson gson = new Gson();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path)))) {

            bufferedWriter.write(gson.toJson(archivedMeasures));
            isSent = true;

        } catch (IOException e) {


            System.out.println("Ooops sth went wrong :(");
            e.printStackTrace();
        }
        return isSent;
    }

}
