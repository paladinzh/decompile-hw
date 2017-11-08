package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

public interface FragmentIntersectionFinder {
    long[] sampleNumbers(Track track, Movie movie);
}
