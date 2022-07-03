package Application;

import SaveToJson.SendToJson;
import WeatherDataManagement.WeatherData;
import Observable.Csi;

import java.util.*;

public class UserInterface {
    private final App app;
    private Scanner sc;

    public UserInterface(App app, Scanner sc) {
        this.app = app;
        this.sc = sc;
    }

    protected <E> void printList(List<E> list, boolean withIndex) {
        if (withIndex) {
            int i = 0;
            for (E e : list) {
                System.out.println("{" + ++i + "} " + e);
            }
        } else {
            for (E e : list) {
                System.out.println(e);
            }
        }
    }

    protected void subscribedListHasSth() {

        if (app.getUser().getSubedLocalisations().isEmpty()) {
            System.out.println("Subscribe first!");
            menu();
        }
    }

    protected int subsListOptions(int version) {
        sc = new Scanner(System.in);
        int a = -1;
        if (app.getUser().getSubedLocalisations().size() != 1) {
            printList(app.getUser().getSubedLocalisations(), true);

            if (version == 1) {
                System.out.println("{" + (app.getUser().getSubedLocalisations().size() + 1) + "} Return to menu");
                a = sc.nextInt();
                if (a == (app.getUser().getSubedLocalisations().size() + 1)) menu();
            }
            if (version == 2) {
                System.out.println("{" + (app.getUser().getSubedLocalisations().size() + 1) + "} Return");
                a = sc.nextInt();
                if (a == (app.getUser().getSubedLocalisations().size() + 1)) seeMeasurements();
            }
        } else {
            a = 1;
        }
        return a - 1;
    }

    protected String unitPrinter(String value) {
        String unit = null;
        if (value.equals("humidity")) unit = "%";
        if (value.equals("temperature")) unit = "°C";
        if (value.equals("pressure")) unit = "hPa";
        return unit;
    }


    protected void open() {
        sc = new Scanner(System.in);
        System.out.println("Welcome to:");
        System.out.println("_____________________________");
        System.out.println("---------The WeatherApp---------");
        System.out.println("-----------------------------");
        System.out.println("The best weather app on the market!");
        System.out.println("Username:");
        app.setUser(new User(sc.nextLine(), new LinkedList<>()));
        System.out.println("Use numbers to navigate");
        System.out.println();
        menu();
    }

    protected void menu() {

        sc = new Scanner(System.in);
        System.out.println("Welcome " + app.getUser().getUsername());
        if (!app.getUser().getSubedLocalisations().isEmpty()) {
            System.out.println("Your subs: ");
            printList(app.getUser().getSubedLocalisations(), false);
        } else System.out.println("You're not subscribed yet!");
        System.out.println("--------");
        System.out.println("--MENU--");
        System.out.println("--------");
        System.out.println("{1} Subscribe to new localisation");
        System.out.println("{2} Unsubscribe");
        System.out.println("{3} See measurements");
        System.out.println("{4} Save measurements");
        System.out.println("{5} Exit");
        try {
            int a = sc.nextInt();
            switch (a) {

                case 1:
                    subSequence();
                    break;
                case 2:
                    subscribedListHasSth();
                    unSubSequence();
                    break;
                case 3:
                    subscribedListHasSth();
                    seeMeasurements();
                    break;
                case 4:
                    subscribedListHasSth();
                    saveToFile();
                    break;
                case 5:
                    app.getUser().makeSaveAndClear(new LinkedList<>(app.getUser().getSubedLocalisations()));
                    System.out.println("Exiting...");
                    break;

                default: {
                    throw new InputMismatchException();
                }
            }

        } catch (InputMismatchException e) {
            System.out.println("Insert proper data!");
            menu();
        }

    }

    protected void subSequence() {
        sc = new Scanner(System.in);
        List<String> toChoose = new ArrayList<>(Csi.Localisation.getLocalisationsList().keySet());
        toChoose.sort(String::compareTo);
        printList(toChoose, true);
        System.out.println("{" + (toChoose.size() + 1) + "} Return to menu");
        try {
            int a = sc.nextInt() - 1;
            if (a == toChoose.size()) menu();
            if (app.getUser().getSubedLocalisations().contains(toChoose.get(a))) {
                System.out.println("You are already subscribed to this location");
                subSequence();
            }
            app.subscribe(toChoose.get(a));
            System.out.println("Success!");
        } catch (IndexOutOfBoundsException | InputMismatchException e) {
            System.out.println("Insert proper data! ");
            subSequence();
        }
        menu();


    }

    protected void unSubSequence() {
        int a = subsListOptions(1);
        try {
            app.unSubscribe(app.getUser().getSubedLocalisations().get(a));
            System.out.println("Success!");

        } catch (IndexOutOfBoundsException | InputMismatchException f) {
            System.out.println("Insert proper data!");
            subSequence();
        }
        menu();

    }

    protected void seeMeasurements() {
        sc = new Scanner(System.in);
        System.out.println("{1} Values");
        System.out.println("{2} Recent measurements");
        System.out.println("{3} All measurements");
        System.out.println("{4} Return to menu");
        try {
            int a = sc.nextInt();
            switch (a) {

                case 1:
                    valuesSequence();
                    break;
                case 2:
                    printRecentMeasurements();
                    break;
                case 3:
                    printAllMeasurements();
                    break;
                case 4:
                    menu();
                    break;
                default: {
                    throw new InputMismatchException();
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Insert proper data");
            seeMeasurements();
        }
    }

    protected void valuesSequence() {
        sc = new Scanner(System.in);
        List<String> variables = List.of("temperature", "humidity", "pressure");
        try {
            String place = app.getUser().getSubedLocalisations().get(subsListOptions(2));
            printList(variables, true);
            String variable = variables.get(sc.nextInt() - 1);


            System.out.println("Average value of " + variable + " in " + place + ": ");
            double average = app.calculateAverage(place, variable, Csi.Localisation.getLocalisationsList());
            if (average == app.getUser().hashCode()) {
                System.out.print("UNAVAILABLE");
            } else {
                if (variable.equals(variables.get(0))) System.out.printf("%.2f%s", average, unitPrinter(variable));
                if (variable.equals(variables.get(1)))
                    System.out.printf("%.2f%s", (average * 100), unitPrinter(variable));
                if (variable.equals(variables.get(2))) System.out.printf("%.2f%s", average, unitPrinter(variable));
            }
            System.out.println();

            System.out.println("Maximum value of " + variable + " in " + place + ": ");
            List<WeatherData> dataListMax = app.getMaxMin(place, variable, 1, Csi.Localisation.getLocalisationsList());
            valuePrinter(dataListMax, variable);

            System.out.println("Minimum value of " + variable + " in " + place + ":");
            List<WeatherData> dataListMin = app.getMaxMin(place, variable, -1, Csi.Localisation.getLocalisationsList());
            valuePrinter(dataListMin, variable);

            System.out.println();


        } catch (InputMismatchException | IndexOutOfBoundsException g) {
            System.out.println("Insert proper data!");
            valuesSequence();
        }
        seeMeasurements();
    }

    protected void valuePrinter(List<WeatherData> weatherDataList, String variable) {
        if (weatherDataList != null) {
            for (WeatherData weatherData : weatherDataList) {
                String timePrint = weatherData.getCollectionTime();
                double value = weatherData.genericGetter(variable);
                if (variable.equals("humidity")) {
                    value *= 100;
                }
                String valuePrint = String.format("%.2f", value);
                System.out.println(timePrint + ": " + valuePrint + unitPrinter(variable));
            }
            System.out.println();
        } else {
            System.out.println("UNAVAILABLE");
        }
    }

    protected void printRecentMeasurements() {
        try {
            int a = subsListOptions(2);

            String key = app.getUser().getSubedLocalisations().get(a);
            System.out.println(app.getLatestMeasures().get(key));

        } catch (InputMismatchException | IndexOutOfBoundsException h) {
            System.out.println("Input proper data!");
            printRecentMeasurements();
        }
        seeMeasurements();
    }

    protected void printAllMeasurements() {
        try {

            int a = subsListOptions(2);
            List<WeatherData> printList = app.getArchivedMeasures().get(app.getUser().getSubedLocalisations().get(a));
            printList.sort(new Comparator<>() {
                @Override
                public int compare(WeatherData w1, WeatherData w2) {
                    int a = w1.getLocalisation().compareTo(w2.getLocalisation());
                    if (a == 0) a = w1.getCollectionTime().compareTo(w2.getCollectionTime());
                    return a;
                }
            });
            printList(printList, false);

        } catch (IndexOutOfBoundsException | InputMismatchException i) {
            System.out.println("Input proper data!");
            printAllMeasurements();
        }
        seeMeasurements();
    }

    protected void saveToFile() {
        sc = new Scanner(System.in);
        System.out.println("Give path:"); //C:\\Users\\zdzic\\IdeaProjects\\WeatherAppProjectObserver\\src\\Application\\saved_weather_data.json
        String path = sc.nextLine();
        System.out.println("________");
        if (SendToJson.toJson(app.getArchivedMeasures(), path)) System.out.println("Sending: Success");
        else System.out.println("Error");
        System.out.println("________");
        System.out.println();
        menu();
    }


}
