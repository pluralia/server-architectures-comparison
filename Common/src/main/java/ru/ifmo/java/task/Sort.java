package ru.ifmo.java.task;

import java.util.ArrayList;
import java.util.List;

public class Sort {
    public static List<Integer> bubbleSort(List<Integer> arr) {
        List<Integer> copy = new ArrayList<>(arr);

        int n = copy.size();
        int tmp = 0;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (copy.get(j) > copy.get(j + 1)) {
                    tmp = copy.get(j);
                    copy.set(j, copy.get(j + 1));
                    copy.set(j + 1, tmp);
                }
            }
        }

        return copy;
    }
}
