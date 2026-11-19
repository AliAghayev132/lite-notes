package az.lite.core;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tərcümə (az / en / ru). Mənbə mətn azərbaycancadır: L.t("Bağla") → seçilmiş dildə.
 * Standart dil telefonun dilinə görədir: az → az, ru → ru, qalan hamısı → en.
 */
public final class L {

    public static final String[] CODES = {"en", "az", "ru"};
    public static final String[] NAMES = {"English", "Azərbaycanca", "Русский"};

    private static String lang = "en";
    private static boolean ready;
    private static final Map<String, String[]> T = new HashMap<>();

    public static void p(String az, String en, String ru) { T.put(az, new String[]{en, ru}); }

    static {
        p("Ləğv et", "Cancel", "Отмена");
        p("Saxla", "Save", "Сохранить");
        p("Sil", "Delete", "Удалить");
        p("Qeyd", "Note", "Заметка");
        p("Siyahı", "List", "Список");
        p("Bərkidildi", "Pinned", "Закреплено");
        p("Bərkitmə götürüldü", "Unpinned", "Откреплено");
        p("Rəng", "Color", "Цвет");
        p("Başlıq", "Title", "Заголовок");
        p("+ Bənd əlavə et", "+ Add item", "+ Добавить пункт");
        p("Qeydini yaz…", "Write your note…", "Напишите заметку…");
        p("Bənd", "Item", "Пункт");
        p("Standart", "Default", "Обычный");
        p("Sarı", "Yellow", "Жёлтый");
        p("Yaşıl", "Green", "Зелёный");
        p("Mavi", "Blue", "Синий");
        p("Bənövşəyi", "Purple", "Фиолетовый");
        p("Qırmızı", "Red", "Красный");
        p("Boz", "Gray", "Серый");
        p("Paylaş", "Share", "Поделиться");
        p("Kopyala", "Copy", "Копировать");
        p("Kopyalandı", "Copied", "Скопировано");
        p("Silinsin?", "Delete?", "Удалить?");
        p("Bu qeyd birdəfəlik silinəcək.", "This note will be deleted permanently.", "Заметка будет удалена навсегда.");
        p("Qeydlərdə axtar…", "Search notes…", "Поиск по заметкам…");
        p("Boş qeyd", "Empty note", "Пустая заметка");
        p("Bərkitməni götür", "Unpin", "Открепить");
        p("Bərkit", "Pin", "Закрепить");
        p("Qeyd yoxdur.\nAşağıdan yeni qeyd və ya siyahı yarat.", "No notes yet.\nCreate a note or a list below.", "Заметок пока нет.\nСоздайте заметку или список ниже.");
        p("+ Siyahı", "+ List", "+ Список");
        p("+ Qeyd", "+ Note", "+ Заметка");
        p("Hamısını .md faylı kimi ixrac et", "Export all as .md file", "Экспорт всех в файл .md");
        p("Ehtiyat nüsxə saxla", "Save backup", "Сохранить резервную копию");
        p("Ehtiyat nüsxədən bərpa et", "Restore from backup", "Восстановить из копии");
        p("İxrac olundu", "Exported", "Экспортировано");
        p("Ehtiyat nüsxə saxlanıldı", "Backup saved", "Копия сохранена");
        p("Bərpa edilsin?", "Restore?", "Восстановить?");
        p("İndiki qeydlər ehtiyat nüsxə ilə əvəz olunacaq.", "Current notes will be replaced by the backup.", "Текущие заметки будут заменены копией.");
        p("Bərpa et", "Restore", "Восстановить");
        p("Bərpa olundu", "Restored", "Восстановлено");
        p("Fayl uyğun deyil", "File is not valid", "Файл не подходит");
        p("Alınmadı", "Failed", "Не удалось");
        p("Qeyd yoxdur. Toxun və yaz.", "No notes. Tap to write.", "Заметок нет. Нажмите, чтобы написать.");
        p("Bərkidilmiş qeyd yoxdur", "No pinned note", "Нет закреплённой заметки");
        p("Dil", "Language", "Язык");
    }

    public static String systemDefault() {
        String sys = Locale.getDefault().getLanguage();
        if ("az".equals(sys)) return "az";
        if ("ru".equals(sys) || "uk".equals(sys) || "be".equals(sys)) return "ru";
        return "en";
    }

    public static void init(Context c) {
        lang = c.getSharedPreferences("lang", Context.MODE_PRIVATE).getString("lang", systemDefault());
        ready = true;
    }

    public static void set(Context c, String code) {
        lang = code;
        c.getSharedPreferences("lang", Context.MODE_PRIVATE).edit().putString("lang", code).commit();
    }

    public static String lang() { return lang; }

    public static Locale locale() {
        if (!ready) return new Locale("az");
        switch (lang) {
            case "az": return new Locale("az");
            case "ru": return new Locale("ru");
            default: return Locale.ENGLISH;
        }
    }

    public static String t(String az) {
        if (!ready || "az".equals(lang) || az == null) return az;
        boolean bang = false;
        String[] v = T.get(az);
        if (v == null && az.startsWith("!")) {
            v = T.get(az.substring(1));
            bang = true;
        }
        if (v == null) return az;
        String r = "en".equals(lang) ? v[0] : v[1];
        return bang && !r.startsWith("!") ? "!" + r : r;
    }

    public static String[] arr(String... az) {
        String[] out = new String[az.length];
        for (int i = 0; i < az.length; i++) out[i] = t(az[i]);
        return out;
    }

    private L() { }
}
