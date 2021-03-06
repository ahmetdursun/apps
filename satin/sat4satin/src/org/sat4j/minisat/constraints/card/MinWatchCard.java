/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.sat4j.minisat.constraints.card;

import java.io.Serializable;

import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class MinWatchCard implements Constr, Undoable, Serializable {

    private static final long serialVersionUID = 1L;

    public static final boolean ATLEAST = true;

    public static final boolean ATMOST = false;

    /**
     * degree of the cardinality constraint
     */
    protected int degree;

    /**
     * literals involved in the constraint
     */
    private int[] lits;

    /**
     * contains the sign of the constraint : ATLEAT or ATMOST
     */
    private boolean moreThan;

    /**
     * contains the sum of the coefficients of the watched literals
     */
    protected int watchCumul;

    /**
     * Vocabulary of the constraint
     */
    private final ILits voc;

    /**
     * Constructs and normalizes a cardinality constraint. used by
     * minWatchCardNew in the non-normalized case.
     * 
     * @param voc
     *            vocabulary used by the constraint
     * @param ps
     *            literals involved in the constraint
     * @param moreThan
     *            should be ATLEAST or ATMOST;
     * @param degree
     *            degree of the constraint
     */
    public MinWatchCard(ILits voc, IVecInt ps, boolean moreThan, int degree) {
        // On met en place les valeurs
        this.voc = voc;
        this.degree = degree;
        this.moreThan = moreThan;

        // On simplifie ps
        int[] index = new int[voc.nVars() * 2 + 2];
        for (int i = 0; i < index.length; i++)
            index[i] = 0;
        // On repertorie les litt?raux utiles
        for (int i = 0; i < ps.size(); i++) {
            if (index[ps.get(i) ^ 1] == 0) {
                index[ps.get(i)]++;
            } else {
                index[ps.get(i) ^ 1]--;
            }
        }
        // On supprime les litt?raux inutiles
        int ind = 0;
        while (ind < ps.size()) {
            if (index[ps.get(ind)] > 0) {
                index[ps.get(ind)]--;
                ind++;
            } else {
                // ??
                if ((ps.get(ind) & 1) != 0)
                    this.degree--;
                ps.set(ind, ps.last());
                ps.pop();
            }
        }

        // On copie les litt?raux de la contrainte
        lits = new int[ps.size()];
        ps.moveTo(lits);

        // On normalise la contrainte au sens de Barth
        normalize();

    }

    /**
     * Constructs and normalizes a cardinality constraint. used by
     * MinWatchCardPB.normalizedMinWatchCardNew() in the normalized case. <br />
     * <strong>Should not be used if parameters are not already normalized</strong><br />
     * This constraint is always an ATLEAST constraint.
     * 
     * @param voc
     *            vocabulary used by the constraint
     * @param ps
     *            literals involved in the constraint
     * @param moreThan
     *            true si la contrainte est sup?rieure ou ?gale
     * @param degree
     *            degree of the constraint
     */
    protected MinWatchCard(ILits voc, IVecInt ps, int degree) {
        // On met en place les valeurs
        this.voc = voc;
        this.degree = degree;
        this.moreThan = ATLEAST;

        // On copie les litt?raux de la contrainte
        lits = new int[ps.size()];
        ps.moveTo(lits);

    }

    /**
     * computes the reason for a literal
     * 
     * @param p
     *            falsified literal (or Lit.UNDEFINED)
     * @param outReason
     *            the reason to be computed. Vector of literals.
     * @see Constr#calcReason(int p, IVecInt outReason)
     */
    public void calcReason(int p, IVecInt outReason) {
        // TODO calcReason: v?rifier par rapport ? l'article
        // Pour chaque litt?ral
        for (int i = 0; i < lits.length; i++) {
            // Si il est falsifi?
            if (voc.isFalsified(lits[i])) {
                // On ajoute sa n?gation au vecteur
                outReason.push(lits[i] ^ 1);
            }
        }
    }

    /**
     * Returns the activity of the constraint
     * 
     * @return activity value of the constraint
     * @see Constr#getActivity()
     */
    public double getActivity() {
        // TODO getActivity
        return 0;
    }

    /**
     * Increments activity of the constraint
     * 
     * @param claInc
     *            value to be added to the activity of the constraint
     * @see Constr#incActivity(double claInc)
     */
    public void incActivity(double claInc) {
        // TODO incActivity
    }

    /**
     * Returns wether the constraint is learnt or not.
     * 
     * @return false : a MinWatchCard cannot be learnt.
     * @see Constr#learnt()
     */
    public boolean learnt() {
        return false;
    }

    /**
     * Simplifies the constraint w.r.t. the assignments of the literals
     * 
     * @param voc
     *            vocabulary used
     * @param ps
     *            literals involved
     * @return value to be added to the degree. This value is less than or equal
     *         to 0.
     */
    protected static int linearisation(ILits voc, IVecInt ps) {
        // Stockage de l'influence des modifications
        int modif = 0;

        for (int i = 0; i < ps.size();) {
            // on verifie si le litteral est affecte
            if (voc.isUnassigned(ps.get(i))) {
                i++;
            } else {
                // Si le litteral est satisfait,
                // ?a revient ? baisser le degr?
                if (voc.isSatisfied(ps.get(i))) {
                    modif--;
                }
                // dans tous les cas, s'il est assign?,
                // on enleve le ieme litteral
                ps.set(i, ps.last());
                ps.pop();
            }
        }

        // DLB: inutile?
        // ps.shrinkTo(nbElement);
        assert modif <= 0;

        return modif;
    }

    /**
     * Returns if the constraint is the reason for a unit propagation.
     * 
     * @return true
     * @see Constr#locked()
     */
    public boolean locked() {
        // TODO locked
        return true;
    }

    /**
     * Constructs a cardinality constraint with a minimal set of watched
     * literals Permet la cr?ation de contrainte de cardinalit? ? observation
     * minimale
     * 
     * @param s
     *            tool for propagation
     * @param voc
     *            vocalulary used by the constraint
     * @param ps
     *            literals involved in the constraint
     * @param moreThan
     *            sign of the constraint. Should be ATLEAST or ATMOST.
     * @param degree
     *            degree of the constraint
     * @return a new cardinality constraint, null if it is a tautology
     * @throws ContradictionException
     */
    public static MinWatchCard minWatchCardNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, boolean moreThan, int degree)
            throws ContradictionException {

        int mydegree = degree + linearisation(voc, ps);

        if (ps.size() == 0 && mydegree > 0) {
            throw new ContradictionException();
        } else if (ps.size() == mydegree || ps.size() <= 0) {
            for (int i = 0; i < ps.size(); i++)
                if (!s.enqueue(ps.get(i))) {
                    throw new ContradictionException();
                }
            return null;
        }

        // La contrainte est maintenant cr??e
        MinWatchCard retour = new MinWatchCard(voc, ps, moreThan, mydegree);

        if (retour.degree <= 0)
            return null;

        retour.computeWatches();

        retour.computePropagation(s);

        return retour;
    }

    /**
     * normalize the constraint (cf. P.Barth normalization)
     */
    public final void normalize() {
        // Gestion du signe
        if (!moreThan) {
            // On multiplie le degr? par -1
            this.degree = 0 - this.degree;
            // On r?vise chaque litt?ral
            for (int indLit = 0; indLit < lits.length; indLit++) {
                lits[indLit] = lits[indLit] ^ 1;
                this.degree++;
            }
            this.moreThan = true;
        }
    }

    /**
     * propagates the value of a falsified literal
     * 
     * @param s
     *            tool for literal propagation
     * @param p
     *            falsified literal
     * @return false if an inconistency is detected, else true
     */
    public boolean propagate(UnitPropagationListener s, int p) {

        // Si la contrainte est responsable de propagation unitaire
        if (watchCumul == degree) {
            voc.watch(p, this);
            return false;
        }

        // Recherche du litt?ral falsifi?
        int indFalsified = 0;
        while ((lits[indFalsified] ^ 1) != p)
            indFalsified++;
        assert watchCumul > degree;

        // Recherche du litt?ral swap
        int indSwap = degree + 1;
        while (indSwap < lits.length && voc.isFalsified(lits[indSwap]))
            indSwap++;

        // Mise ? jour de la contrainte
        if (indSwap == lits.length) {
            // Si aucun litt?ral n'a ?t? trouv?
            voc.watch(p, this);
            // La limite est atteinte
            watchCumul--;
            assert watchCumul == degree;
            voc.undos(p).push(this);

            // On met en queue les litt?raux impliqu?s
            for (int i = 0; i <= degree; i++)
                if ((p != (lits[i] ^ 1)) && !s.enqueue(lits[i], this))
                    return false;

            return true;
        }
        // Si un litt?ral a ?t? trouv? on les ?change
        int tmpInt = lits[indSwap];
        lits[indSwap] = lits[indFalsified];
        lits[indFalsified] = tmpInt;

        // On observe le nouveau litt?ral
        voc.watch(tmpInt ^ 1, this);

        return true;
    }

    /**
     * Removes a constraint from the solver
     */
    public void remove() {
        for (int i = 0; i <= degree; i++) {
            voc.watches(lits[i] ^ 1).remove(this);
        }
    }

    /**
     * Rescales the activity value of the constraint
     * 
     * @param d
     *            rescale factor
     */
    public void rescaleBy(double d) {
        // TODO rescaleBy
    }

    /**
     * simplifies the constraint
     * 
     * @return true if the constraint is satisfied, else false
     */
    public boolean simplify() {
        // Calcul de la valeur actuelle
        for (int i = 0, count = 0; i < lits.length; i++)
            if (voc.isSatisfied(lits[i]) && (++count == degree))
                return true;

        return false;
    }

    /**
     * Returns a string representation of the constraint.
     * 
     * @return representation of the constraint.
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        stb.append("Card (" + lits.length + ") : ");
        if (lits.length > 0) {
            // if (voc.isUnassigned(lits[0])) {
            stb.append(Lits.toString(this.lits[0]));
            stb.append("[");
            stb.append(voc.valueToString(lits[0]));
            stb.append("@");
            stb.append(voc.getLevel(lits[0]));
            stb.append("]");
            stb.append(" "); //$NON-NLS-1$
            // }
            for (int i = 1; i < lits.length; i++) {
                // if (voc.isUnassigned(lits[i])) {
                stb.append(" + "); //$NON-NLS-1$
                stb.append(Lits.toString(this.lits[i]));
                stb.append("[");
                stb.append(voc.valueToString(lits[i]));
                stb.append("@");
                stb.append(voc.getLevel(lits[i]));
                stb.append("]");
                stb.append(" "); //$NON-NLS-1$
                // }
            }
            stb.append(">= "); //$NON-NLS-1$
            stb.append(this.degree);
        }
        return stb.toString();
    }

    /**
     * Updates information on the constraint in case of a backtrack
     * 
     * @param p
     *            unassigned literal
     */
    public void undo(int p) {
        // Le litt?ral observ? et falsifi? devient non assign?
        watchCumul++;
    }

    public void setLearnt() {
        throw new UnsupportedOperationException();
    }

    public void register() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return lits.length;
    }

    public int get(int i) {
        return lits[i];
    }

    public void assertConstraint(UnitPropagationListener s) {
        throw new UnsupportedOperationException();
    }

    protected void computeWatches() {
        int indSwap = lits.length;
        int tmpInt;
        for (int i = 0; i <= degree && i < indSwap; i++) {
            while (voc.isFalsified(lits[i]) && --indSwap > i) {
                tmpInt = lits[i];
                lits[i] = lits[indSwap];
                lits[indSwap] = tmpInt;
            }

            // Si le litteral est observable
            if (!voc.isFalsified(lits[i])) {
                watchCumul++;
                voc.watch(lits[i] ^ 1, this);
            }
        }
        if (learnt()) {
            // chercher tous les litteraux a regarder
            // par ordre de niveau decroissant
            int free = 1;
            while ((watchCumul <= degree) && (free > 0)) {
                free = 0;
                // regarder le litteral falsifie au plus bas niveau
                int maxlevel = -1, maxi = -1;
                for (int i = watchCumul; i < lits.length; i++) {
                    if (voc.isFalsified(lits[i])) {
                        free++;
                        int level = voc.getLevel(lits[i]);
                        if (level > maxlevel) {
                            maxi = i;
                            maxlevel = level;
                        }
                    }
                }
                if (free > 0) {
                    assert maxi >= 0;
                    voc.watch(lits[maxi] ^ 1, this);
                    tmpInt = lits[maxi];
                    lits[maxi] = lits[watchCumul];
                    lits[watchCumul] = tmpInt;
                    watchCumul++;
                    free--;
                    assert free >= 0;
                }
            }
            assert lits.length == 1 || watchCumul > 1;
        }

    }

    protected MinWatchCard computePropagation(UnitPropagationListener s)
            throws ContradictionException {

        // Si on a des litteraux impliques
        if (watchCumul == degree) {
            for (int i = 0; i < lits.length; i++)
                if (!s.enqueue(lits[i])) {
                    throw new ContradictionException();
                }
            return null;
        }

        // Si on n'observe pas suffisamment
        if (watchCumul < degree) {
            throw new ContradictionException();
        }
        return this;
    }

    public int[] getLits() {
        int[] tmp = new int[size()];
        System.arraycopy(lits, 0, tmp, 0, size());
        return tmp;
    }

    public ILits getVocabulary() {
        return voc;
    }

    // SATIN
    public void setLearntGlobal() {
        throw new UnsupportedOperationException();
    }

    public boolean learntGlobal() {
        return false;
    }
}
