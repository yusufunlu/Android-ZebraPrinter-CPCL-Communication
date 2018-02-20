package com.zebra.isv.scanandpairbluetooth;

/**
 * Created by DTYUNLU on 14.02.2018.
 */

public enum PrintFormats {

    NORMAL("Normal"),
    BOLD("Bold"),
    CENTERED_NORMAL("Centered"),
    CENTERED_BOLD("Centered Bold"),
    RIGHT_BIG("right Big"),
    BARCODE("Barcode");
    private final String text;

    private PrintFormats(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
