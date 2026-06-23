package com.fsocial.postservice.util;

import com.fsocial.postservice.entity.Account;

public final class DisplayNameUtils {

    private DisplayNameUtils() {
    }

    public static String build(String lastName, String firstName) {
        String last = lastName == null ? "" : lastName.trim();
        String first = firstName == null ? "" : firstName.trim();
        if (last.isEmpty()) return first;
        if (first.isEmpty()) return last;
        return last + " " + first;
    }

    public static String build(Account account) {
        if (account == null) return "";
        return build(account.getLastName(), account.getFirstName());
    }
}
