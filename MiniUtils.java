import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 小さいユーティリティメソッド集
 */
public class MiniUtils {

    public static final String EMPTY = "";

    /**
     * 引数{@code src}がnullであったら空文字列を返す。そうでなければ、{@code src.toString()}を返す
     * 
     * @param src
     * @return
     */
    public static String n2e(Object src) {
        return (src == null) ? EMPTY : src.toString();
    }

    /**
     * 引数{@code src}がnullまたは空文字列の場合はtrue、そうでなければfalseを返す
     * 
     * @param src
     * @return
     */
    public static Boolean isEmpty(String src) {
        return (src == null || src.isEmpty());
    }

    /** formatDateRFC1123メソッド用のSimpleDateFormatは、スレッド内で再利用できるよう ThreadLocalに保持する */
    // ※Format系クラスがスレッドセーフでないため、スレッドを超えた共有はしない。
    private static final ThreadLocal<SimpleDateFormat> rfc1123Form = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat v = new SimpleDateFormat(
                    "E, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);
            v.setTimeZone(TimeZone.getTimeZone("GMT"));
            return v;
        }
    };

    /**
     * Dateの日付・時刻をRFC1123書式の文字列に変換する。
     * 
     * @param date
     * @return
     */
    public static String formatDateRFC1123(Date date) {
        return rfc1123Form.get().format(date);
    }

    /**
     * 二要素のObject配列の配列から、Mapを生成する。<br>
     * 使用例：<br>
     * 
     * <pre>
     * {@code 
     * Object[][] data = 
     *     new Object[][]{"key1","value1"},{"key2","value2"}}; 
     * Map<String, String> map = buildMap(data, String.class, String.class); 
     * }
     * </pre>
     * 
     * @param data
     *            Mapに設定する二要素配列の配列
     * @param keyType
     *            Mapのキー項目の型
     * @param keyType
     *            Mapの値項目の型
     * @return
     * @exception ClassCastException
     *                keyType, valueTypeと異なるキー、値がdataに含まれていた場合に発生する。
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> buildMap(Object[][] data, Class<K> keyType,
            Class<V> valueType) {
        Map<K, V> map = new HashMap<K, V>();
        Map<K, V> cMap = Collections.checkedMap(map, keyType, valueType);

        for (Object[] e : data) {
            cMap.put((K) e[0], (V) e[1]);
        }
        return map;
    }

}
