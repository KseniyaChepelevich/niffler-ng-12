package guru.qa.niffler.config;

import java.util.Locale;

public enum Browser {
    CHROME,
    FIREFOX;

    public String browserName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
