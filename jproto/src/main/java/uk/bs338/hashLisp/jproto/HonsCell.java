package uk.bs338.hashLisp.jproto;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* HonsCells are mutable in memoEval, but this is not included in the hashValue or the objectHash */
public class HonsCell {
    private int objectHash;
    @Nonnull
    private final LispValue fst, snd;
    /* mutable */
    @Nullable
    private LispValue memoEval;
    private final String special;
    private int collision;

    /* for special values */
    public HonsCell(int objectHash, String special) {
        this.objectHash = objectHash;
        this.fst = LispValue.nil;
        this.snd = LispValue.nil;
        this.memoEval = null; /* XXX or nil? */
        this.special = special;
        this.collision = 0;
    }
    
    /* for special values */
    public HonsCell(@Nonnull LispValue special)  {
        this.objectHash = special.toObjectHash();
        this.fst = this.snd = LispValue.nil;
        this.memoEval = null; /* XXX or nil? */
        this.special = special.getSpecialName();
        this.collision = 0;
    }

    public HonsCell(@Nonnull LispValue fst, @Nonnull LispValue snd) {
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

    public int bumpObjectHash() {
        collision++;
        calcObjectHash();
        return this.objectHash;
    }

    public int getObjectHash() {
        return objectHash;
    }

    public LispValue getMemoEval() {
        return memoEval;
    }

    @Nonnull
    public LispValue getFst() {
        return fst;
    }

    @Nonnull
    public LispValue getSnd() {
        return snd;
    }

    public String getSpecial() {
        return special;
    }

    public void setMemoEval(LispValue memoEval) {
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
        if (objectHash != other.objectHash)
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (this.special != null)
            return "HonsCell{objectHash=" + objectHash + ", special=" + special + "}";
        return "HonsCell{objectHash=" + objectHash + ", memoEval=" + memoEval + ", fst=" + fst + ", snd=" + snd + ", collision=" + collision + "}";
    }

    @Nonnull
    public LispValue toValue() {
        return LispValue.fromObjectHash(objectHash);
    }
}
