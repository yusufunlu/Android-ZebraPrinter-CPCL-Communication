package com.zebra.isv.scanandpairbluetooth;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dtyunlu on 27.06.2016.
 */
public class GenericTable {

    @SerializedName("Kod")
    private String _kod;
    public String getKod() {
        return _kod;
    }

    public GenericTable(String _ad) {
        this._ad = _ad;
    }

    public GenericTable() {
    }

    public GenericTable(String _kod, String _ad) {
        this._kod = _kod;
        this._ad = _ad;
    }

    public void setKod(String value) {
        _kod = value;
    }

    @SerializedName("Ad")
    private String _ad;
    public String getAd() {
        return _ad;
    }

    public void setAd(String value) {
        _ad = value;
    }

}
