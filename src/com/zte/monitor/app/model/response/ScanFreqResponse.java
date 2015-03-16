package com.zte.monitor.app.model.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-3.
 */
public class ScanFreqResponse extends UdpResponse {

    public byte cellIdx;
    public byte cellsSrcCount;
    public short selectSCell;
    public short selectNCell;
    public List<Byte> cellsCount = new ArrayList<Byte>();
    public List<Byte> cellsCountRxLev = new ArrayList<Byte>();
    public List<Entry1> entry1List = new ArrayList<Entry1>();

    public static class Entry1 implements Serializable {
        public byte netCuler;
        public byte baseCuler;
        public byte scellC1;
        public byte scellC2;
        public byte scellRxLev;
        public byte bestCellsCount;
        public byte nCellsCount;
        public List<Short> bestCells = new ArrayList<Short>();
        public short serverCell;
        public short lac;
        public int cellId;
        public List<Entry2> entry2List = new ArrayList<Entry2>();
    }

    public static class Entry2 implements Serializable {
        public byte c1;
        public byte c2;
        public byte rxlev;
        public byte bsic;
        public short nCell;
        public short nLac;
        public int nCellId;
        public boolean isChecked;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry2)) return false;

            Entry2 entry2 = (Entry2) o;

            if (bsic != entry2.bsic) return false;
            if (c1 != entry2.c1) return false;
            if (c2 != entry2.c2) return false;
            if (nCell != entry2.nCell) return false;
            if (nCellId != entry2.nCellId) return false;
            if (nLac != entry2.nLac) return false;
            if (rxlev != entry2.rxlev) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) c1;
            result = 31 * result + (int) c2;
            result = 31 * result + (int) rxlev;
            result = 31 * result + (int) bsic;
            result = 31 * result + (int) nCell;
            result = 31 * result + (int) nLac;
            result = 31 * result + nCellId;
            return result;
        }
    }
}
