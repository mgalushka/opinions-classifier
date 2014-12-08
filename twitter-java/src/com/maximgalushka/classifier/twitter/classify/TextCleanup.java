package com.maximgalushka.classifier.twitter.classify;

/**
 * @author Maxim Galushka
 */
public final class TextCleanup {

    /**
     * <ul>
     * <li>Removes all "RT" asking for retweet.</li>
     * <li>Removes all the mentions.</li>
     * <li>Clean-up all the urls</li>
     * </ul>
     */
    public static String reformatMessage(String initial) {
        String formatted = initial.replaceAll("(r|R)(t|T)", "");
        formatted = formatted.replaceAll("@\\S+", "");
        formatted = formatted.replaceAll("http[s]?:[/]{1,2}\\S*", "");

        // remove all URLs' remains
        formatted = formatted.replaceAll("https:", "");
        formatted = formatted.replaceAll("http:", "");
        formatted = formatted.replaceAll("https", "");
        formatted = formatted.replaceAll("http", "");

        // normalize internal spaces
        formatted = formatted.replaceAll("\\s+", " ");
        return formatted.trim();
    }
}
