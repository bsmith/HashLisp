package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;

import java.util.Objects;

/* HonsCells are mutable in memoEval, but this is not included in the hashValue or the objectHash */
public class HonsCell {
    private int objectHash;
    @NotNull
    private final HonsValue fst, snd;
    /* mutable */
    @Nullable
    private HonsValue memoEval;
    
    /* for special values */
    public HonsCell(@NotNull HonsValue special)  {
        this.objectHash = special.toObjectHash();
        this.fst = this.snd = HonsValue.nil;
        this.memoEval = null; /* XXX or nil? */
    }

    public HonsCell(@NotNull HonsValue fst, @NotNull HonsValue snd) {
        this.fst = fst;
        this.snd = snd;
        this.memoEval = null; /* XXX or nil? maybe it evaluates to nil */
        calcObjectHash();
    }
    
    private int hashFunction(int fst, int snd)
    {
        return Objects.hash(fst, snd);
    }

    /* The complexity is that this must not be zero, and is signed int31 */
    private void calcObjectHash() {
        /* XXX sign bit */
        this.objectHash = hashFunction(fst.getValue(), snd.getValue()) & 0x3fffffff;
        while (this.objectHash == 0)
            bumpObjectHash();
    }

    public void bumpObjectHash() {
        this.objectHash = hashFunction(this.objectHash, 0) & 0x3fffffff;
    }

    public int getObjectHash() {
        return objectHash;
    }

    public @Nullable HonsValue getMemoEval() {
        return memoEval;
    }

    @NotNull
    public HonsValue getFst() {
        return fst;
    }

    @NotNull
    public HonsValue getSnd() {
        return snd;
    }
    
    public @NotNull ConsPair<HonsValue> getPair() { return ConsPair.of(fst, snd); }

    public @Nullable String getSpecial() {
        return HonsValue.fromObjectHash(objectHash).getSpecialName();
    }

    public void setMemoEval(@Nullable HonsValue memoEval) {
        this.memoEval = memoEval;
    }

    @Override
    public int hashCode() {
        return this.objectHash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HonsCell other = (HonsCell) obj;
        if (!fst.equals(other.fst))
            return false;
        if (!snd.equals(other.snd))
            return false;
        return objectHash == other.objectHash;
    }

    @Override
    public @NotNull String toString() {
        var special = getSpecial();
        if (special != null)
            return "HonsCell{objectHash=0x" + Integer.toHexString(objectHash) + ", special=" + special + "}";
        return "HonsCell{objectHash=0x" + Integer.toHexString(objectHash) + ", memoEval=" + memoEval + ", fst=" + fst + ", snd=" + snd + "}";
    }

    @NotNull
    public HonsValue toValue() {
        return HonsValue.fromObjectHash(objectHash);
    }
}
