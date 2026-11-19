package az.lite.core;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/** Rəngli kvadrat + ortada 1-2 hərf: fayl növləri, kateqoriyalar üçün yüngül ikon. */
public final class Glyph extends Drawable {
    private final Paint bg = new Paint(), fg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String text;

    public Glyph(String text, int bgColor, int fgColor) {
        this.text = text;
        bg.setColor(bgColor);
        fg.setColor(fgColor);
        fg.setTypeface(Ui.BOLD);
        fg.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas c) {
        Rect b = getBounds();
        c.drawRect(b, bg);
        fg.setTextSize(b.height() * (text.length() > 2 ? 0.30f : 0.40f));
        float y = b.centerY() - (fg.descent() + fg.ascent()) / 2;
        c.drawText(text, b.centerX(), y, fg);
    }

    @Override public void setAlpha(int a) { }
    @Override public void setColorFilter(ColorFilter cf) { }
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}
