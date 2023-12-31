package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;

import java.util.Objects;

/* HonsCells are mutable in memoEval, but this is not included in the hashValue or the objectHash */
public class HonsCell {
    private int objectHash;
    /* XXX these should be ints like objectHash!  then memoEval should use a sentinel not null */
    @NotNull
    private final HonsValue fst, snd;
    /* mutable */
    @Nullable
    private HonsValue memoEval;
    
    /* for special values */
    public HonsCell(@NotNull HonsValue special)  {
        this.objectHash = special.toObjectHash();
        this.fst = this.snd = HonsValue.nil;
        this.memoEval = null;
    }

    public HonsCell(@NotNull HonsValue fst, @NotNull HonsValue snd) {
        this.fst = fst;
        this.snd = snd;
        this.memoEval = null;
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
        if (this.objectHash == 0 || this.objectHash == 1)
            bumpObjectHash();
    }

    public void bumpObjectHash() {
        do {
            this.objectHash = hashFunction(this.objectHash, 0) & 0x3fffffff;
        } while (this.objectHash == 0 || this.objectHash == 1);
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
        String body = special != null ?
            "special=" + special :
            "memoEval=" + memoEval + ", fst=" + fst + ", snd=" + snd;
        return "HonsCell{objectHash=0x" + Integer.toHexString(objectHash) + " (#" + objectHash + "), " + body + "}";
    }

    @NotNull
    public HonsValue toValue() {
        return HonsValue.fromObjectHash(objectHash);
    }
}
