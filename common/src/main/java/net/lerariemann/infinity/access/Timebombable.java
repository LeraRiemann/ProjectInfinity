package net.lerariemann.infinity.access;

public interface Timebombable {
    int cooldownTicks = 6000;

    void infinity$timebomb();
    boolean infinity$tryRestore();

    boolean infinity$isTimebombed();
    int infinity$getTimebombProgress();
}
