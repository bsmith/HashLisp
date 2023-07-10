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
    private final String special;
    private int collision;
    
    /* for special values */
    public HonsCell(@NotNull HonsValue special)  {
        this.objectHash = special.toObjectHash();
        this.fst = this.snd = HonsValue.nil;
        this.memoEval = null; /* XXX or nil? */
        this.special = special.getSpecialName();
        this.collision = 0;
    }

    public HonsCell(@NotNull HonsValue fst, @NotNull HonsValue snd) {
        this.fst = fst;
        this.snd = snd;
        this.memoEval = null; /* XXX or nil? maybe it evaluates to nil */
        this.special = null;
        this.collision = 0;
        calcObjectHash();
    }

    /* The complexity is that this must not be zero, and is signed int31 */
    private void calcObjectHash() {
        /* XXX sign bit */
        var newHash = Objects.hash(fst, snd, collision) & 0x3fffffff;
        while (newHash == 0)
            newHash = Objects.hash(newHash) & 0x3fffffff;
        this.objectHash = newHash;
    }

    public void bumpObjectHash() {
        collision++;
        calcObjectHash();
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
    
    public ConsPair<HonsValue> getPair() { return ConsPair.of(fst, snd); }

    public String getSpecial() {
        return special;
    }

    public void setMemoEval(@NotNull HonsValue memoEval) {
        this.memoEval = memoEval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.objectHash, this.fst, this.snd);
    }

    @Override
    public boolean equals(Object obj) {
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
    public String toString() {
        if (this.special != null)
            return "HonsCell{objectHash=" + objectHash + ", special=" + special + "}";
        return "HonsCell{objectHash=" + objectHash + ", memoEval=" + memoEval + ", fst=" + fst + ", snd=" + snd + ", collision=" + collision + "}";
    }

    @NotNull
    public HonsValue toValue() {
        return HonsValue.fromObjectHash(objectHash);
    }
}
