package com.ada.federate.cache;

import java.util.*;

public class BinarySign {
    private List<String> unionSet;

    public List<Boolean> runEvenList;
    public List<Boolean> evenFlagList;
    private List<Integer> l, r, threshold;
    public int size;

    public BinarySign(int boundS, int boundE, List<String> unionSet) {
        this.size = unionSet.size();
        evenFlagList = new ArrayList<>(Collections.nCopies(size, false));
        runEvenList = new ArrayList<>(Collections.nCopies(size, false));
        this.unionSet = new ArrayList<>(unionSet);
        threshold = new ArrayList<>(Collections.nCopies(size, 0));

        l = new ArrayList<>(Collections.nCopies(size, boundS));
        r = new ArrayList<>(Collections.nCopies(size, boundE));
    }

    public void remove(int index) {
        l.remove(index);
        r.remove(index);
        threshold.remove(index);
        evenFlagList.remove(index);
        runEvenList.remove(index);
        unionSet.remove(index);
        size--;
    }

    public void remove(List<Integer> indices) {
        if (indices.size() == 0) return;
        // 该方法时间消耗主要为 6 次 newList的add，每次为 O(n)，而原来的remove方法每次也是O(n)，当删除的元素超过6个时，该方法时间复杂度优于原始方法，但考虑到新方法还存在空间开销，所以权衡两者，当超过20个时使用新方法
        if (indices.size() <= 20) {
            // 对待删除的索引进行排序（从大到小）
            indices.sort(Collections.reverseOrder());
            for (int i = 0; i < indices.size(); i++) {
                int indexToRemove = indices.get(i);
                remove(indexToRemove);
            }
        }
        Set<Integer> indexSet = new HashSet<>(indices);
        List<String> newUnionSet = new ArrayList<>();
        List<Boolean> newEvenFlagList = new ArrayList<>();
        List<Boolean> newRunEvenList = new ArrayList<>();
        List<Integer> newL = new ArrayList<>();
        List<Integer> newR = new ArrayList<>();
        List<Integer> newThreshold = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (!indexSet.contains(i)) {
                newUnionSet.add(unionSet.get(i));
                newEvenFlagList.add(evenFlagList.get(i));
                newRunEvenList.add(runEvenList.get(i));
                newL.add(l.get(i));
                newR.add(r.get(i));
                newThreshold.add(threshold.get(i));
            }
        }

        unionSet = newUnionSet;
        evenFlagList = newEvenFlagList;
        runEvenList = newRunEvenList;
        l = newL;
        r = newR;
        threshold = newThreshold;
        size = unionSet.size();
    }

    public Integer l(int index) {
        return l.get(index);
    }

    public Integer r(int index) {
        return r.get(index);
    }

    public Integer threshold(int index) {
        return threshold.get(index);
    }

    public String unionSet(int index) {
        return unionSet.get(index);
    }


    public List<Integer> getThreshold() {
        return threshold;
    }

    public List<String> getUnionSet() {
        return unionSet;
    }

    public String bound2String() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append("[").append(l(i)).append(",").append(r(i)).append("]");
            if (i != size - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public void set_threshold(int index, int threshold) {
        this.threshold.set(index, threshold);
    }

    public void set_l(int index, int val) {
        this.l.set(index, val);
    }

    public void set_evenFlag(int index, boolean val) {
        this.evenFlagList.set(index, val);
    }

    public void set_runEven(int index, boolean val) {
        this.runEvenList.set(index, val);
    }


    public void set_r(int index, int val) {
        this.r.set(index, val);
    }


    /**
     * l 数组 是否全部小于 r
     *
     * @return
     */
    public boolean judge() {
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) < r.get(i))
                return true;
        }
        return false;
    }
}