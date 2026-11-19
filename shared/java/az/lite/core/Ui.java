package az.lite.core;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;

/** Lite Launcher-in öz dizayn sistemi: düz, solid rənglər, künc radiusu yoxdur. */
public final class Ui {

    public static final int SCRIM = 0xB3000000;
    public static final int SURFACE = 0xFF121212;
    public static final int SURFACE2 = 0xFF1F1F1F;
    public static final int SURFACE3 = 0xFF2A2A2A;
    public static final int LINE = 0xFF333333;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT2 = 0xFF9A9A9A;
    public static final int ACCENT = 0xFFFFD60A;
    public static final int ON_ACCENT = 0xFF000000;
    public static final int DANGER = 0xFFFF453A;

    public static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    public static final Typeface BOLD = Typeface.create("sans-serif", Typeface.BOLD);

    private static final Locale AZ = new Locale("az");

    public static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static GradientDrawable solid(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        return g;
    }

    public static GradientDrawable outline(int fill, int stroke, int strokePx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setStroke(strokePx, stroke);
        return g;
    }

    public static StateListDrawable pressable(int normal, int pressed) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        s.addState(new int[]{}, new ColorDrawable(normal));
        return s;
    }

    /** Düymə: primary = sarı fon + qara yazı, secondary = tünd boz fon + ağ yazı. */
    public static TextView button(Context c, String text, boolean primary) {
        TextView b = new TextView(c);
        b.setText(L.t(text).toUpperCase(L.locale()));
        b.setTypeface(BOLD);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        b.setLetterSpacing(0.06f);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(c, 46));
        b.setPadding(dp(c, 18), 0, dp(c, 18), 0);
        if (primary) {
            b.setTextColor(ON_ACCENT);
            b.setBackground(pressable(ACCENT, 0xFFE0BC00));
        } else {
            b.setTextColor(TEXT);
            b.setBackground(pressable(SURFACE3, LINE));
        }
        b.setClickable(true);
        return b;
    }

    /** Siyahı sətri. */
    public static TextView row(Context c, CharSequence text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextColor(TEXT);
        t.setTypeface(MEDIUM);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setMinHeight(dp(c, 50));
        t.setPadding(dp(c, 14), 0, dp(c, 14), 0);
        t.setBackground(pressable(SURFACE2, SURFACE3));
        t.setClickable(true);
        return t;
    }

    public static EditText input(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        e.setTextColor(TEXT);
        e.setHintTextColor(TEXT2);
        e.setTypeface(MEDIUM);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        e.setBackground(outline(SURFACE2, LINE, dp(c, 1)));
        e.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        return e;
    }

    public static TextView title(Context c, String s) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextColor(TEXT);
        t.setTypeface(BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        t.setPadding(0, 0, 0, dp(c, 14));
        return t;
    }

    public static TextView section(Context c, String s) {
        TextView t = new TextView(c);
        t.setText(L.t(s).toUpperCase(L.locale()));
        t.setTextColor(TEXT2);
        t.setTypeface(BOLD);
        t.setLetterSpacing(0.08f);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setPadding(0, dp(c, 16), 0, dp(c, 8));
        return t;
    }

    /** Kvadrat seçim qutusu (siyahı sətirlərində sol tərəfdə). */
    public static Drawable checkbox(Context c, boolean on) {
        int s = dp(c, 18);
        GradientDrawable g = on ? solid(ACCENT) : outline(0, TEXT2, dp(c, 2));
        g.setBounds(0, 0, s, s);
        return g;
    }

    /** Native Toast əvəzinə: aşağıda qısa müddətli solid zolaq. */
    public static void toast(final FrameLayout root, String text) {
        Context c = root.getContext();
        final TextView t = new TextView(c);
        t.setText(L.t(text));
        t.setTextColor(ON_ACCENT);
        t.setTypeface(BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setBackgroundColor(ACCENT);
        t.setPadding(dp(c, 18), dp(c, 12), dp(c, 18), dp(c, 12));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        lp.bottomMargin = dp(c, 56);
        root.addView(t, lp);
        t.setAlpha(0f);
        t.animate().alpha(1f).setDuration(120).start();
        t.postDelayed(() -> t.animate().alpha(0f).setDuration(160)
                .withEndAction(() -> root.removeView(t)).start(), 1600);
    }

    public static View spacer(Context c, int wDp, int hDp) {
        View v = new View(c);
        v.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(c, wDp), dp(c, hDp)));
        return v;
    }

    private Ui() { }
}
