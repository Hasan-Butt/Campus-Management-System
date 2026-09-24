package com.fast.campus.comparator;

import com.fast.campus.model.Student;
import java.util.Comparator;

/**
 * Sorts students alphabetically by name (A → Z).
 */
public class StudentNameComparator implements Comparator<Student> {
    @Override
    public int compare(Student o1, Student o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}
