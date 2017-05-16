package org.hcjf.utils;

/**
 * Object
 * @author javaito.
 */
public final class Version implements Comparable<Version> {

    private static final String BETA = "Beta";
    private static final String RELEASE_CANDIDATE = "RC";

    private final Integer releaseGroup;
    private final Integer releaseNumber;
    private final Integer buildNumber;
    private final Boolean beta;
    private final Boolean releaseCandidate;
    private final String representation;

    public Version(Integer releaseGroup, Integer releaseNumber, Integer buildNumber, Boolean beta, Boolean releaseCandidate) {
        this.releaseGroup = releaseGroup;
        this.releaseNumber = releaseNumber;
        this.buildNumber = buildNumber;
        this.beta = beta;
        this.releaseCandidate = releaseCandidate;

        StringBuilder builder = new StringBuilder();
        builder.append(releaseGroup).append(Strings.CLASS_SEPARATOR);
        builder.append(releaseNumber).append(Strings.CLASS_SEPARATOR);
        builder.append(buildNumber);

        if(beta) {
            builder.append(Strings.WHITE_SPACE).append(BETA);
        } else if(releaseCandidate) {
            builder.append(Strings.WHITE_SPACE).append(RELEASE_CANDIDATE);
        }

        representation = builder.toString();
    }

    public Integer getReleaseGroup() {
        return releaseGroup;
    }

    public Integer getReleaseNumber() {
        return releaseNumber;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    public Boolean getBeta() {
        return beta;
    }

    public Boolean getReleaseCandidate() {
        return releaseCandidate;
    }

    @Override
    public String toString() {
        return representation;
    }

    @Override
    public int compareTo(Version o) {
        return (releaseGroup + releaseNumber + buildNumber) -
                (o.releaseGroup + o.releaseNumber + o.buildNumber);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof Version) {
            result = compareTo((Version) obj) == 0;
        }
        return result;
    }

    public static Version build(String version) {
        String[] numbers = version.split("\\" + Strings.CLASS_SEPARATOR);

        Integer releaseGroup = 0;
        Integer releaseNumber = 0;
        Integer buildNumber = 0;
        Boolean beta = false;
        Boolean releaseCandidate = false;

        if(numbers[numbers.length - 1].endsWith(BETA)) {
            beta = true;
            numbers[numbers.length - 1] = numbers[numbers.length - 1].replace(BETA, Strings.EMPTY_STRING).trim();
        }

        if(numbers[numbers.length - 1].endsWith(RELEASE_CANDIDATE)) {
            releaseCandidate = true;
            numbers[numbers.length - 1] = numbers[numbers.length - 1].replace(RELEASE_CANDIDATE, Strings.EMPTY_STRING);
        }

        if(numbers.length > 3){
            throw new IllegalArgumentException("");
        } else if(numbers.length > 2) {
            releaseGroup = Integer.parseInt(numbers[0]);
            releaseNumber = Integer.parseInt(numbers[1]);
            buildNumber = Integer.parseInt(numbers[2]);
        } else if(numbers.length > 1) {
            releaseGroup = Integer.parseInt(numbers[0]);
            releaseNumber = Integer.parseInt(numbers[1]);
        } else if(numbers.length > 0) {
            releaseGroup = Integer.parseInt(numbers[0]);
        }

        return new Version(releaseGroup, releaseNumber, buildNumber, beta, releaseCandidate);
    }
}
