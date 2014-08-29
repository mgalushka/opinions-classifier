package com.maximgalushka.classifier.twitter.clusters;

import com.google.gson.annotations.SerializedName;
import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 8/29/2014.
 */
public class Clusters {

    @SerializedName("statuses")
    private List<Tweet> clusters = new ArrayList<Tweet>();
}
