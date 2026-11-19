package az.lite.core;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Ümumi siyahı: ikon + başlıq + alt yazı + sağ yazı. */
public class Rows<T> extends BaseAdapter {

    public interface Binder<T> { void bind(Row r, T item, int position); }

    public static final class Row extends LinearLayout {
        public final ImageView icon;
        public final TextView title, sub, right;

        public Row(Context c) {
            super(c);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            int p = Ui.dp(c, 16);
            setPadding(p, Ui.dp(c, 8), p, Ui.dp(c, 8));
            setMinimumHeight(Ui.dp(c, 60));
            setBackground(Ui.pressable(0, Ui.SURFACE2));
            icon = new ImageView(c);
            icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LayoutParams ilp = new LayoutParams(Ui.dp(c, 44), Ui.dp(c, 44));
            ilp.rightMargin = Ui.dp(c, 14);
            addView(icon, ilp);
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(VERTICAL);
            title = new TextView(c);
            title.setTextColor(Ui.TEXT);
            title.setTypeface(Ui.MEDIUM);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            title.setSingleLine(true);
            title.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
            sub = new TextView(c);
            sub.setTextColor(Ui.TEXT2);
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            sub.setSingleLine(true);
            sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
            col.addView(title);
            col.addView(sub);
            addView(col, new LayoutParams(0, -2, 1));
            right = new TextView(c);
            right.setTextColor(Ui.TEXT2);
            right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            right.setPadding(Ui.dp(c, 10), 0, 0, 0);
            addView(right);
        }

        public Row icon(Drawable d) {
            icon.setVisibility(d == null ? GONE : VISIBLE);
            icon.setImageDrawable(d);
            return this;
        }
    }

    public final List<T> items = new ArrayList<>();
    private final Binder<T> binder;

    public Rows(Binder<T> binder) { this.binder = binder; }

    @Override public int getCount() { return items.size(); }
    @Override public T getItem(int p) { return items.get(p); }
    @Override public long getItemId(int p) { return p; }

    @Override
    public View getView(int p, View cv, ViewGroup parent) {
        Row r = cv instanceof Row ? (Row) cv : new Row(parent.getContext());
        r.icon.setVisibility(View.VISIBLE);
        r.icon.setImageDrawable(null);
        r.icon.setBackground(null);
        r.icon.setPadding(0, 0, 0, 0);
        r.title.setTextColor(Ui.TEXT);
        r.right.setTextColor(Ui.TEXT2);
        binder.bind(r, items.get(p), p);
        return r;
    }

    public void set(List<T> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public static ListView list(Context c) {
        ListView l = new ListView(c);
        l.setDivider(null);
        l.setSelector(new android.graphics.drawable.ColorDrawable(0));
        l.setOverScrollMode(View.OVER_SCROLL_NEVER);
        l.setVerticalScrollBarEnabled(false);
        l.setClipToPadding(false);
        return l;
    }
}
