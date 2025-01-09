package com.septi.resq.utils;

import com.septi.resq.R;
import com.septi.resq.model.QuickAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DummyData {
    private static final List<QuickAction> ALL_ACTIONS;
    static {
        ALL_ACTIONS = new ArrayList<QuickAction>();
        ALL_ACTIONS.add(new QuickAction("Lapor Kecelakaan", R.drawable.ic_accident, "119"));
        ALL_ACTIONS.add(new QuickAction("Panggil Ambulans", R.drawable.ic_ambulance, "118"));
        ALL_ACTIONS.add(new QuickAction("Hubungi Polisi", R.drawable.ic_police, "110"));
        ALL_ACTIONS.add(new QuickAction("Pemadam Kebakaran", R.drawable.ic_fire, "113"));
        ALL_ACTIONS.add(new QuickAction("Lapor Bencana", R.drawable.ic_disaster, "115"));
        ALL_ACTIONS.add(new QuickAction("Pertolongan Pertama", R.drawable.ic_first_aid, "119"));
        ALL_ACTIONS.add(new QuickAction("Evakuasi Segera", R.drawable.ic_evacuate, "115"));
        ALL_ACTIONS.add(new QuickAction("Bantuan Medis", R.drawable.ic_accident, "119"));
        ALL_ACTIONS.add(new QuickAction("Pencarian Orang", R.drawable.ic_search, "110"));
        ALL_ACTIONS.add(new QuickAction("Tim SAR", R.drawable.ic_accident, "115"));
    }

    public static List<QuickAction> getQuickActions() {
        List<QuickAction> quickActions = new ArrayList<>();
        for (int i = 0; i < Math.min(7, ALL_ACTIONS.size()); i++) {
            QuickAction action = ALL_ACTIONS.get(i);
            quickActions.add(new QuickAction(
                    truncateText(action.getTitle()),
                    action.getIconResource(),
                    action.getPhoneNumber()
            ));
        }
        quickActions.add(new QuickAction("Lihat Semua", R.drawable.ic_chevron_right, null));
        return quickActions;
    }

    public static List<QuickAction> getAllActions() {
        return new ArrayList<>(ALL_ACTIONS);
    }

    private static String truncateText(String text) {
        int maxLength = 14;
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }
}