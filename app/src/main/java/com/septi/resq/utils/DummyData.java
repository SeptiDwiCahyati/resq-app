package com.septi.resq.utils;

import com.septi.resq.R;
import com.septi.resq.model.QuickAction;
import com.septi.resq.model.Report;
import com.septi.resq.model.RescueTeam;

import java.util.ArrayList;
import java.util.List;

public class DummyData {

    public static List<QuickAction> getQuickActions() {
        List<QuickAction> quickActions = new ArrayList<>();
        quickActions.add(new QuickAction(truncateText("Lapor Kecelakaan"), R.drawable.ic_accident));
        quickActions.add(new QuickAction(truncateText("Panggil Ambulans"), R.drawable.ic_ambulance));
        quickActions.add(new QuickAction(truncateText("Hubungi Polisi"), R.drawable.ic_police));
        quickActions.add(new QuickAction(truncateText("Pemadam Kebakaran"), R.drawable.ic_fire));
        quickActions.add(new QuickAction(truncateText("Lapor Bencana"), R.drawable.ic_disaster));
        quickActions.add(new QuickAction(truncateText("Pertolongan Pertama"), R.drawable.ic_first_aid));
        quickActions.add(new QuickAction(truncateText("Evakuasi Segera"), R.drawable.ic_evacuate));
        quickActions.add(new QuickAction(truncateText("Lihat Semua"), R.drawable.ic_chevron_right));
        return quickActions;
    }

    private static String truncateText(String text) {
        int maxLength = 14;
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        } else {
            return text;
        }
    }


    public static List<QuickAction> getAllActions() {
        List<QuickAction> allActions = new ArrayList<>();
        allActions.add(new QuickAction("Lapor Kecelakaan", R.drawable.ic_accident));
        allActions.add(new QuickAction("Panggil Ambulans", R.drawable.ic_ambulance));
        allActions.add(new QuickAction("Hubungi Polisi", R.drawable.ic_police));
        allActions.add(new QuickAction("Pemadam Kebakaran", R.drawable.ic_fire));
        allActions.add(new QuickAction("Pertolongan Pertama", R.drawable.ic_fire));
        return allActions;
    }
}
