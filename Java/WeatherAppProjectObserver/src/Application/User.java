package Application;


import java.util.LinkedList;
import java.util.List;


public class User {

    private final List<String> subedLocalisations;
    private List<String> savedLocalisations;
    private final String username;

    public User(String username, List<String> subedLocalisations) {
        this.subedLocalisations = subedLocalisations;
        this.username = username;

    }

    protected void makeSaveAndClear(List<String> list) {
        this.savedLocalisations = new LinkedList<>(list);
        subedLocalisations.clear();
    }

    public List<String> getSavedLocalisations() {
        return savedLocalisations;
    }

    protected String getUsername() {
        return username;
    }

    public List<String> getSubedLocalisations() {
        return subedLocalisations;
    }

}
