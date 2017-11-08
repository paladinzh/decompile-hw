package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.boxes.TimeToSampleBox.Entry;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import java.util.Arrays;
import java.util.List;

public class TwoSecondIntersectionFinder implements FragmentIntersectionFinder {
    protected long getDuration(Track track) {
        long duration = 0;
        for (Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    public long[] sampleNumbers(Track track, Movie movie) {
        List<Entry> entries = track.getDecodingTimeEntries();
        double trackLength = 0.0d;
        for (Track thisTrack : movie.getTracks()) {
            double thisTracksLength = (double) (getDuration(thisTrack) / thisTrack.getTrackMetaData().getTimescale());
            if (trackLength < thisTracksLength) {
                trackLength = thisTracksLength;
            }
        }
        int fragmentCount = ((int) Math.ceil(trackLength / 2.0d)) - 1;
        if (fragmentCount < 1) {
            fragmentCount = 1;
        }
        long[] fragments = new long[fragmentCount];
        Arrays.fill(fragments, -1);
        fragments[0] = 1;
        long time = 0;
        int samples = 0;
        for (Entry entry : entries) {
            int i = 0;
            while (((long) i) < entry.getCount()) {
                int currentFragment = ((int) ((time / track.getTrackMetaData().getTimescale()) / 2)) + 1;
                if (currentFragment >= fragments.length) {
                    break;
                }
                int samples2 = samples + 1;
                fragments[currentFragment] = (long) (samples + 1);
                time += entry.getDelta();
                i++;
                samples = samples2;
            }
        }
        long last = (long) (samples + 1);
        for (i = fragments.length - 1; i >= 0; i--) {
            if (fragments[i] == -1) {
                fragments[i] = last;
            }
            last = fragments[i];
        }
        return fragments;
    }
}
