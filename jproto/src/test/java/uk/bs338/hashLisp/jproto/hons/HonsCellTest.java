package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.ConsPair;

import static org.junit.jupiter.api.Assertions.*;

public class HonsCellTest {
    @Test void canCreateCellForNil() {
        HonsCell cell = new HonsCell(HonsValue.nil);
        assertEquals(0, cell.getObjectHash());
        assertEquals(HonsValue.nil, cell.getFst());
        assertEquals(HonsValue.nil, cell.getSnd());
        assertNull(cell.getMemoEval());
        assertEquals("nil", cell.getSpecial());
    }

    @Test void canCreateCellForSymbolTag() {
        HonsCell cell = new HonsCell(HonsValue.symbolTag);
        assertEquals(1, cell.getObjectHash());
        assertEquals(HonsValue.nil, cell.getFst());
        assertEquals(HonsValue.nil, cell.getSnd());
        assertNull(cell.getMemoEval());
        assertEquals("symbol", cell.getSpecial());
    }
    
    @Test void canCreateCellForPair() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        assertTrue(cell.getObjectHash() != 0 && cell.getObjectHash() != 1);
        assertEquals(HonsValue.fromSmallInt(1), cell.getFst());
        assertEquals(HonsValue.fromSmallInt(2), cell.getSnd());
        assertNull(cell.getMemoEval());
    }
    
    @Test void canSetAndGetMemoEval() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        assertNull(cell.getMemoEval());
        cell.setMemoEval(HonsValue.fromSmallInt(3));
        assertEquals(HonsValue.fromSmallInt(3), cell.getMemoEval());
    }
    
    @Test void equalsExcludesMemoEval() {
        HonsCell cell1 = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        HonsCell cell2 = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        assertEquals(cell1, cell2);
        cell2.setMemoEval(HonsValue.fromSmallInt(3));
        assertEquals(cell1, cell2);
        assertEquals(cell1.hashCode(), cell2.hashCode());
    }
    
    @Test void canConvertToValue() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        HonsValue expected = HonsValue.fromObjectHash(cell.getObjectHash());
        assertEquals(expected, cell.toValue());
    }
    
    @Test void canConvertToString() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        String actual = cell.toString();
        assertTrue(actual.contains("HonsCell"));
        assertTrue(actual.contains(String.valueOf(cell.getObjectHash())));
    }
    
    @Test void toStringDescribesSpecials() {
        HonsCell nilCell = new HonsCell(HonsValue.nil);
        HonsCell symbolTagCell = new HonsCell(HonsValue.symbolTag);
        assertTrue(nilCell.toString().contains("nil"));
        assertTrue(symbolTagCell.toString().contains("symbol"));
    }
    
    @Test void canGetAsPair() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        ConsPair<HonsValue> pair = cell.getPair();
        assertEquals(HonsValue.fromSmallInt(1), pair.fst());
        assertEquals(HonsValue.fromSmallInt(2), pair.snd());
    }
    
    @Test void canBumpObjectHash() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        var firstHash = cell.getObjectHash();
        cell.bumpObjectHash();
        var secondHash = cell.getObjectHash();
        assertNotEquals(firstHash, secondHash);
    }
}
