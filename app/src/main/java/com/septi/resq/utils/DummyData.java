package com.septi.resq.utils;

import com.septi.resq.R;
import com.septi.resq.model.QuickAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DummyData {
    // Single source of actions data
    private static final List<QuickAction> ALL_ACTIONS;
    static {
        ALL_ACTIONS = new ArrayList<QuickAction>();
        ALL_ACTIONS.add(new QuickAction("Lapor Kecelakaan", R.drawable.ic_accident));
        ALL_ACTIONS.add(new QuickAction("Panggil Ambulans", R.drawable.ic_ambulance));
        ALL_ACTIONS.add(new QuickAction("Hubungi Polisi", R.drawable.ic_police));
        ALL_ACTIONS.add(new QuickAction("Pemadam Kebakaran", R.drawable.ic_fire));
        ALL_ACTIONS.add(new QuickAction("Lapor Bencana", R.drawable.ic_disaster));
        ALL_ACTIONS.add(new QuickAction("Pertolongan Pertama", R.drawable.ic_first_aid));
        ALL_ACTIONS.add(new QuickAction("Evakuasi Segera", R.drawable.ic_evacuate));
        ALL_ACTIONS.add(new QuickAction("Bantuan Medis", R.drawable.ic_accident));
        ALL_ACTIONS.add(new QuickAction("Pencarian Orang", R.drawable.ic_search));
        ALL_ACTIONS.add(new QuickAction("Tim SAR", R.drawable.ic_accident));
    }

    public static List<QuickAction> getQuickActions() {
        List<QuickAction> quickActions = new ArrayList<>();
        // Ambil 7 item pertama
        for (int i = 0; i < Math.min(7, ALL_ACTIONS.size()); i++) {
            QuickAction action = ALL_ACTIONS.get(i);
            quickActions.add(new QuickAction(
                    truncateText(action.getTitle()),
                    action.getIconResource()
            ));
        }
        // Tambah tombol "Lihat Semua"
        quickActions.add(new QuickAction("Lihat Semua", R.drawable.ic_chevron_right));
        return quickActions;
    }

    public static List<QuickAction> getAllActions() {
        return new ArrayList<>(ALL_ACTIONS); // Return semua actions
    }

    private static String truncateText(String text) {
        int maxLength = 14;
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }
}