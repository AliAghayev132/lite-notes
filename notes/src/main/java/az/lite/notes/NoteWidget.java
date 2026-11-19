package az.lite.notes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import az.lite.core.L;

import java.util.List;

/** Vidcet: ən üstdəki (bərkidilmiş və ya son) qeydi göstərir. */
public class NoteWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        RemoteViews v = views(c);
        for (int id : ids) m.updateAppWidget(id, v);
    }

    static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, NoteWidget.class));
        if (ids == null || ids.length == 0) return;
        RemoteViews v = views(c);
        for (int id : ids) m.updateAppWidget(id, v);
    }

    private static RemoteViews views(Context c) {
        L.init(c);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_note);
        List<Store.Note> l = Store.get(c).sorted("");
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        Intent open;
        if (l.isEmpty()) {
            v.setTextViewText(R.id.w_title, "Lite Notes");
            v.setTextViewText(R.id.w_body, L.t("Qeyd yoxdur. Toxun və yaz."));
            open = new Intent(c, EditActivity.class);
        } else {
            Store.Note n = l.get(0);
            v.setTextViewText(R.id.w_title, (n.pinned ? "★ " : "") + (n.title.isEmpty() ? NotesActivity.firstLine(n.preview()) : n.title));
            v.setTextViewText(R.id.w_body, n.preview());
            open = new Intent(c, EditActivity.class).putExtra("id", n.id);
        }
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(c, 0, open, flags));
        return v;
    }
}
