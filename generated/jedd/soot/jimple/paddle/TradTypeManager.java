package soot.jimple.paddle;

import soot.*;
import soot.util.*;
import soot.jimple.paddle.queue.*;
import soot.jimple.paddle.bdddomains.*;
import java.util.*;

public class TradTypeManager extends AbsTypeManager {
    TradTypeManager(Rvar vars, Robj allocs, FastHierarchy fh) {
        super(vars, allocs);
        this.fh = fh;
    }
    
    public void update() {
        for (Iterator tIt = vars.iterator(); tIt.hasNext(); ) {
            final Rvar.Tuple t = (Rvar.Tuple) tIt.next();
            VarNode vn = t.var();
            Type type = vn.getType();
            if (typeMask.get(type) != null) continue;
            BitVector bv = new BitVector(allocNodes.size());
            typeMask.put(type, bv);
            for (Iterator anIt = allocNodes.iterator(); anIt.hasNext(); ) {
                final AllocNode an = (AllocNode) anIt.next();
                if (castNeverFails(an.getType(), type)) {
                    bv.set(an.getNumber());
                    change = true;
                }
            }
        }
        for (Iterator tIt = allocs.iterator(); tIt.hasNext(); ) {
            final Robj.Tuple t = (Robj.Tuple) tIt.next();
            AllocNode an = t.obj();
            allocNodes.add(an);
            for (Iterator typeIt = Scene.v().getTypeNumberer().iterator(); typeIt.hasNext(); ) {
                final Type type = (Type) typeIt.next();
                if (!(type instanceof RefLikeType)) continue;
                if (type instanceof AnySubType) continue;
                BitVector bv = (BitVector) typeMask.get(type);
                if (bv == null) continue;
                if (castNeverFails(an.getType(), type)) {
                    bv.set(an.getNumber());
                    change = true;
                }
            }
        }
    }
    
    public BitVector get(Type type) {
        if (type == null) return null;
        update();
        BitVector ret = (BitVector) typeMask.get(type);
        if (ret == null && fh != null) throw new RuntimeException("oops" + type);
        return ret;
    }
    
    private BDDGetter bddGetter;
    
    public jedd.internal.RelationContainer get() {
        if (fh == null)
            return new jedd.internal.RelationContainer(new jedd.Attribute[] {  },
                                                       new jedd.PhysicalDomain[] {  },
                                                       ("return jedd.internal.Jedd.v().trueBDD(); at /tmp/soot-trunk/" +
                                                        "src/soot/jimple/paddle/TradTypeManager.jedd:78,25-31"),
                                                       jedd.internal.Jedd.v().trueBDD());
        if (bddGetter == null) bddGetter = this.new BDDGetter();
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                                   new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                                   ("return bddGetter.get(); at /tmp/soot-trunk/src/soot/jimple/p" +
                                                    "addle/TradTypeManager.jedd:80,8-14"),
                                                   bddGetter.get());
    }
    
    class BDDGetter {
        private final jedd.internal.RelationContainer cachedTypeMasks =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { T1.v(), H1.v() },
                                              ("private <soot.jimple.paddle.bdddomains.type:soot.jimple.padd" +
                                               "le.bdddomains.T1, soot.jimple.paddle.bdddomains.obj> cachedT" +
                                               "ypeMasks = jedd.internal.Jedd.v().falseBDD() at /tmp/soot-tr" +
                                               "unk/src/soot/jimple/paddle/TradTypeManager.jedd:83,16-30"),
                                              jedd.internal.Jedd.v().falseBDD());
        
        private final jedd.internal.RelationContainer cachedVarNodes =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), type.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                              ("private <soot.jimple.paddle.bdddomains.var, soot.jimple.padd" +
                                               "le.bdddomains.type> cachedVarNodes = jedd.internal.Jedd.v()." +
                                               "falseBDD() at /tmp/soot-trunk/src/soot/jimple/paddle/TradTyp" +
                                               "eManager.jedd:84,16-27"),
                                              jedd.internal.Jedd.v().falseBDD());
        
        private final jedd.internal.RelationContainer cachedVarObj =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("private <soot.jimple.paddle.bdddomains.var, soot.jimple.padd" +
                                               "le.bdddomains.obj> cachedVarObj = jedd.internal.Jedd.v().fal" +
                                               "seBDD() at /tmp/soot-trunk/src/soot/jimple/paddle/TradTypeMa" +
                                               "nager.jedd:85,16-26"),
                                              jedd.internal.Jedd.v().falseBDD());
        
        public jedd.internal.RelationContainer get() {
            update();
            if (change) {
                for (Iterator tIt = Scene.v().getTypeNumberer().iterator(); tIt.hasNext(); ) {
                    final Type t = (Type) tIt.next();
                    BitVector mask = (BitVector) typeMask.get(t);
                    if (mask == null) continue;
                    BitSetIterator bsi = mask.iterator();
                    Numberer objNumberer = PaddleNumberers.v().allocNodeNumberer();
                    while (bsi.hasNext()) {
                        int objNum = bsi.next();
                        cachedTypeMasks.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { t, objNumberer.get(objNum) },
                                                                               new jedd.Attribute[] { type.v(), obj.v() },
                                                                               new jedd.PhysicalDomain[] { T1.v(), H1.v() }));
                    }
                }
                cachedVarObj.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(cachedTypeMasks),
                                                               cachedVarNodes,
                                                               new jedd.PhysicalDomain[] { T1.v() }));
                change = false;
            }
            final jedd.internal.RelationContainer varNodes =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), type.v() },
                                                  new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                  ("<soot.jimple.paddle.bdddomains.var:soot.jimple.paddle.bdddom" +
                                                   "ains.V1, soot.jimple.paddle.bdddomains.type:soot.jimple.padd" +
                                                   "le.bdddomains.T1> varNodes = jedd.internal.Jedd.v().falseBDD" +
                                                   "(); at /tmp/soot-trunk/src/soot/jimple/paddle/TradTypeManage" +
                                                   "r.jedd:104,24-32"),
                                                  jedd.internal.Jedd.v().falseBDD());
            while (newVarNodes.hasNext()) {
                VarNode vn = (VarNode) newVarNodes.next();
                varNodes.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { vn, vn.getType() },
                                                                new jedd.Attribute[] { var.v(), type.v() },
                                                                new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
            }
            cachedVarObj.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(cachedTypeMasks),
                                                                varNodes,
                                                                new jedd.PhysicalDomain[] { T1.v() }));
            cachedVarNodes.eqUnion(varNodes);
            return new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v() },
                                                       new jedd.PhysicalDomain[] { H1.v(), V1.v() },
                                                       ("return cachedVarObj; at /tmp/soot-trunk/src/soot/jimple/padd" +
                                                        "le/TradTypeManager.jedd:111,12-18"),
                                                       cachedVarObj);
        }
        
        public BDDGetter() { super(); }
    }
    
    
    public boolean castNeverFails(Type from, Type to) {
        if (fh == null) return true;
        if (to == null) return true;
        if (to == from) return true;
        if (from == null) return false;
        if (to.equals(from)) return true;
        if (from instanceof NullType) return true;
        if (from instanceof AnySubType) return true;
        if (to instanceof NullType) return false;
        if (to instanceof AnySubType) throw new RuntimeException("oops from=" + from + " to=" + to);
        return fh.canStoreType(from, to);
    }
    
    private LargeNumberedMap typeMask = new LargeNumberedMap(Scene.v().getTypeNumberer());
    
    private NumberedSet allocNodes = new NumberedSet(PaddleNumberers.v().allocNodeNumberer());
    
    private Iterator newVarNodes = PaddleNumberers.v().varNodeNumberer().iterator();
    
    private FastHierarchy fh;
    
    private boolean change = false;
}