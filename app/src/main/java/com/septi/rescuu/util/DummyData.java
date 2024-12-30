package com.septi.rescuu.util;

import com.septi.rescuu.R;
import com.septi.rescuu.model.QuickAction;
import com.septi.rescuu.model.Report;
import com.septi.rescuu.model.RescueTeam;

import java.util.ArrayList;
import java.util.List;

public class DummyData {

    public static List<RescueTeam> getActiveTeams() {
        List<RescueTeam> activeTeams = new ArrayList<>();
        activeTeams.add(new RescueTeam("Basarnas Jakarta", "2 km", "Tersedia"));
        activeTeams.add(new RescueTeam("PMI Tangerang", "3.5 km", "Dalam Tugas"));
        activeTeams.add(new RescueTeam("Relawan Indonesia", "5 km", "Tersedia"));
        activeTeams.add(new RescueTeam("SAR Gunung Jaya", "7 km", "Dalam Tugas"));
        activeTeams.add(new RescueTeam("Damkar Bandung", "1 km", "Tersedia"));
        activeTeams.add(new RescueTeam("Tim Evakuasi Surabaya", "8 km", "Tersedia"));
        activeTeams.add(new RescueTeam("Tim Kesehatan Makassar", "4 km", "Dalam Tugas"));
        activeTeams.add(new RescueTeam("Relawan Lombok", "6 km", "Tersedia"));
        return activeTeams;
    }


    public static List<Report> getRecentReports() {
        List<Report> recentReports = new ArrayList<>();
        recentReports.add(new Report("Kecelakaan Motor", "Jl. Sudirman", "10 menit yang lalu"));
        recentReports.add(new Report("Kebakaran", "Jl. Thamrin", "30 menit yang lalu"));
        recentReports.add(new Report("Banjir", "Jl. Gatot Subroto", "1 jam yang lalu"));
        recentReports.add(new Report("Banjir", "Jl. Gatot Subroto", "1 jam yang lalu"));
        recentReports.add(new Report("Banjir", "Jl. Gatot Subroto", "1 jam yang lalu"));
        recentReports.add(new Report("Banjir", "Jl. Gatot Subroto", "1 jam yang lalu"));
        return recentReports;
    }

    public static List<QuickAction> getQuickActions() {
        List<QuickAction> quickActions = new ArrayList<>();
        quickActions.add(new QuickAction(truncateText("Lapor Kecelakaan"), R.drawable.ic_accident));
        quickActions.add(new QuickAction(truncateText("Panggil Ambulans"), R.drawable.ic_ambulance));
        quickActions.add(new QuickAction(truncateText("Hubungi Polisi"), R.drawable.ic_police));
        quickActions.add(new QuickAction(truncateText("Pemadam Kebakaran"), R.drawable.ic_fire));
        quickActions.add(new QuickAction(truncateText("Lapor Bencana"), R.drawable.ic_fire));
        quickActions.add(new QuickAction(truncateText("Pertolongan Pertama"), R.drawable.ic_fire));
        quickActions.add(new QuickAction(truncateText("Evakuasi Segera"), R.drawable.ic_fire));
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
