package az.lite.core;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Bütün Lite tətbiqləri üçün baza ekran: başlıq zolağı + gövdə + öz panellərimiz. */
public abstract class LiteActivity extends Activity {

    public static final int BG = 0xFF0E0E0E;

    protected FrameLayout root;
    protected LinearLayout column;
    protected TextView titleView;
    protected LinearLayout actions;
    protected FrameLayout body;
    protected View header;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            w.setStatusBarColor(BG);
            w.setNavigationBarColor(BG);
        }
        root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setFitsSystemWindows(true);
        root.addView(column, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(16), 0, dp(6), 0);
        View bar = new View(this);
        bar.setBackgroundColor(Ui.ACCENT);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(4), dp(22));
        blp.rightMargin = dp(10);
        h.addView(bar, blp);
        titleView = new TextView(this);
        titleView.setTextColor(Ui.TEXT);
        titleView.setTypeface(Ui.BOLD);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        h.addView(titleView, new LinearLayout.LayoutParams(0, -2, 1));
        actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        h.addView(actions);
        header = h;
        column.addView(h, new LinearLayout.LayoutParams(-1, dp(56)));

        body = new FrameLayout(this);
        column.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    protected void setTitleText(CharSequence t) { titleView.setText(t); }

    /** Başlıqda mətnli düymə. */
    protected TextView action(String label, View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setText(L.t(label));
        t.setTextColor(Ui.TEXT);
        t.setTypeface(Ui.BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), 0, dp(12), 0);
        t.setBackground(Ui.pressable(Color.TRANSPARENT, Ui.SURFACE3));
        t.setOnClickListener(l);
        actions.addView(t, new LinearLayout.LayoutParams(-2, dp(44)));
        return t;
    }

    public int dp(float v) { return Ui.dp(this, v); }

    public void toast(String s) { Ui.toast(root, s); }

    /** true qaytarsa geri düyməsi istifadə olunub. */
    protected boolean onBack() { return false; }

    @Override
    public void onBackPressed() {
        if (Sheet.closeTop()) return;
        if (onBack()) return;
        super.onBackPressed();
    }
}
