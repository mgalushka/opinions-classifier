package com.maximgalushka.classifier.twitter.classify;

/**
 * @author Maxim Galushka
 */
public enum Classification {

    NEUTRAL("n"), UKRAINE("u"), RUSSIA("r");

    private String key;

    Classification(String r) {
        this.key = r;
    }

    public String getKey() {
        return key;
    }

    public static Classification fromKey(String key) {
        if (key == null || "".equals(key.trim())) return NEUTRAL;
        for (Classification c : values()) {
            if (c.getKey().equals(key.trim())) return c;
        }
        return NEUTRAL;
    }
}
