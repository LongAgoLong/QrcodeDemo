package com.google.zxing.oned;

import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;

import java.util.Arrays;

public class UPCEANReader {
    // These two values are critical for determining how permissive the decoding will be.
    // We've arrived at these values through a lot of trial and error. Setting them any higher
    // lets false positives creep in quickly.
    private static final float MAX_AVG_VARIANCE = 0.48f;
    private static final float MAX_INDIVIDUAL_VARIANCE = 0.7f;

    /**
     * Attempts to decode a single UPC/EAN-encoded digit.
     *
     * @param row row of black/white values to decode
     * @param counters the counts of runs of observed black/white/black/... values
     * @param rowOffset horizontal offset to start decoding from
     * @param patterns the set of patterns to use to decode -- sometimes different encodings
     * for the digits 0-9 are used, and this indicates the encodings for 0 to 9 that should
     * be used
     * @return horizontal offset of first pixel beyond the decoded digit
     * @throws NotFoundException if digit cannot be decoded
     */
    static int decodeDigit(BitArray row, int[] counters, int rowOffset, int[][] patterns)
            throws NotFoundException {
        recordPattern(row, rowOffset, counters);
        float bestVariance = MAX_AVG_VARIANCE; // worst variance we'll accept
        int bestMatch = -1;
        int max = patterns.length;
        for (int i = 0; i < max; i++) {
            int[] pattern = patterns[i];
            float variance = patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE);
            if (variance < bestVariance) {
                bestVariance = variance;
                bestMatch = i;
            }
        }
        if (bestMatch >= 0) {
            return bestMatch;
        } else {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    static int[] findGuardPattern(BitArray row,
                                  int rowOffset,
                                  boolean whiteFirst,
                                  int[] pattern) throws NotFoundException {
        return findGuardPattern(row, rowOffset, whiteFirst, pattern, new int[pattern.length]);
    }

    /**
     * @param row row of black/white values to search
     * @param rowOffset position to start search
     * @param whiteFirst if true, indicates that the pattern specifies white/black/white/...
     * pixel counts, otherwise, it is interpreted as black/white/black/...
     * @param pattern pattern of counts of number of black and white pixels that are being
     * searched for as a pattern
     * @param counters array of counters, as long as pattern, to re-use
     * @return start/end horizontal offset of guard pattern, as an array of two ints
     * @throws NotFoundException if pattern is not found
     */
    private static int[] findGuardPattern(BitArray row,
                                          int rowOffset,
                                          boolean whiteFirst,
                                          int[] pattern,
                                          int[] counters) throws NotFoundException {
        int width = row.getSize();
        rowOffset = whiteFirst ? row.getNextUnset(rowOffset) : row.getNextSet(rowOffset);
        int counterPosition = 0;
        int patternStart = rowOffset;
        int patternLength = pattern.length;
        boolean isWhite = whiteFirst;
        for (int x = rowOffset; x < width; x++) {
            if (row.get(x) != isWhite) {
                counters[counterPosition]++;
            } else {
                if (counterPosition == patternLength - 1) {
                    if (patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) < MAX_AVG_VARIANCE) {
                        return new int[]{patternStart, x};
                    }
                    patternStart += counters[0] + counters[1];
                    System.arraycopy(counters, 2, counters, 0, counterPosition - 1);
                    counters[counterPosition - 1] = 0;
                    counters[counterPosition] = 0;
                    counterPosition--;
                } else {
                    counterPosition++;
                }
                counters[counterPosition] = 1;
                isWhite = !isWhite;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    /**
     * Records the size of successive runs of white and black pixels in a row, starting at a given point.
     * The values are recorded in the given array, and the number of runs recorded is equal to the size
     * of the array. If the row starts on a white pixel at the given start point, then the first count
     * recorded is the run of white pixels starting from that point; likewise it is the count of a run
     * of black pixels if the row begin on a black pixels at that point.
     *
     * @param row row to count from
     * @param start offset into row to start at
     * @param counters array into which to record counts
     * @throws NotFoundException if counters cannot be filled entirely from row before running out
     *  of pixels
     */
    protected static void recordPattern(BitArray row,
                                        int start,
                                        int[] counters) throws NotFoundException {
        int numCounters = counters.length;
        Arrays.fill(counters, 0, numCounters, 0);
        int end = row.getSize();
        if (start >= end) {
            throw NotFoundException.getNotFoundInstance();
        }
        boolean isWhite = !row.get(start);
        int counterPosition = 0;
        int i = start;
        while (i < end) {
            if (row.get(i) != isWhite) {
                counters[counterPosition]++;
            } else {
                if (++counterPosition == numCounters) {
                    break;
                } else {
                    counters[counterPosition] = 1;
                    isWhite = !isWhite;
                }
            }
            i++;
        }
        // If we read fully the last section of pixels and filled up our counters -- or filled
        // the last counter but ran off the side of the image, OK. Otherwise, a problem.
        if (!(counterPosition == numCounters || (counterPosition == numCounters - 1 && i == end))) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    protected static void recordPatternInReverse(BitArray row, int start, int[] counters)
            throws NotFoundException {
        // This could be more efficient I guess
        int numTransitionsLeft = counters.length;
        boolean last = row.get(start);
        while (start > 0 && numTransitionsLeft >= 0) {
            if (row.get(--start) != last) {
                numTransitionsLeft--;
                last = !last;
            }
        }
        if (numTransitionsLeft >= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        recordPattern(row, start + 1, counters);
    }

    /**
     * Determines how closely a set of observed counts of runs of black/white values matches a given
     * target pattern. This is reported as the ratio of the total variance from the expected pattern
     * proportions across all pattern elements, to the length of the pattern.
     *
     * @param counters observed counters
     * @param pattern expected pattern
     * @param maxIndividualVariance The most any counter can differ before we give up
     * @return ratio of total variance between counters and pattern compared to total pattern size
     */
    protected static float patternMatchVariance(int[] counters,
                                                int[] pattern,
                                                float maxIndividualVariance) {
        int numCounters = counters.length;
        int total = 0;
        int patternLength = 0;
        for (int i = 0; i < numCounters; i++) {
            total += counters[i];
            patternLength += pattern[i];
        }
        if (total < patternLength) {
            // If we don't even have one pixel per unit of bar width, assume this is too small
            // to reliably match, so fail:
            return Float.POSITIVE_INFINITY;
        }

        float unitBarWidth = (float) total / patternLength;
        maxIndividualVariance *= unitBarWidth;

        float totalVariance = 0.0f;
        for (int x = 0; x < numCounters; x++) {
            int counter = counters[x];
            float scaledPattern = pattern[x] * unitBarWidth;
            float variance = counter > scaledPattern ? counter - scaledPattern : scaledPattern - counter;
            if (variance > maxIndividualVariance) {
                return Float.POSITIVE_INFINITY;
            }
            totalVariance += variance;
        }
        return totalVariance / total;
    }
}
