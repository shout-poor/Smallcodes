import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * パスワードのハッシュ、およびハッシュしたパスワードと平文の一致判定を行うユーティリティクラス。<br>
 * 要:Apache Commons-Lang3, Commons-Codec<br>
 * <br>
 * ハッシュ処理の仕様：ストレッチング(saltを付加し、Digestを取る)を1000回繰り返します。DigestアルゴリズムはSHA-256です
 */
public class PasswordHashUtil {
    
    
    /** ストレッチングを行う回数 */
    private static final int DEFAULT_ITERATE_COUNT = 1000;
    /** saltの長さ */
    private static final int SALT_LENGTH = 64;
    /** saltに利用可能な文字。区切り文字に使用する「$」は除く */
    private static final String SALT_CHARS;
    static {
        String a = "abcdefghijklmnopqrstuvwxyz";
        SALT_CHARS = a + a.toUpperCase() + "_,.!\"#%&'()-=;+:*\\";
    }

    /**
     * 平文パスワードのハッシュにsaltを付加した文字列を返します。<br>
     * 戻り値の文字列は以下のような形式なります。<br>
     * 
     * <pre>
     * [salt]$[ハッシュされたパスワード]
     * </pre>
     * 
     * saltは自動生成（擬似乱数文字列）されます。
     * 
     * @param src 平文パスワード
     * @return 上記形式の文字列
     */
    public static String crypt(String src) {
        return crypt((src == null) ? "" : src, newSalt(SALT_LENGTH),
                DEFAULT_ITERATE_COUNT);
    }

    /**
     * 入力された平文パスワードをハッシュし、hashedと一致するかどうか判定します。 <br>
     * hashedは、{@link #crypt(String)}メソッドで作成されるものと同様、以下の形式になっているものとします。
     * 
     * <pre>
     * [salt]$[ハッシュされたパスワード]
     * </pre>
     * 
     * @param pass 平文パスワード
     * @param hashed 上記形式のパスワードハッシュ
     * @return パスワードが一致すればtrue, そうでなければfalse
     */
    public static boolean passwordMatch(String pass, String hashed) {
        if (hashed == null || hashed.length() < SALT_LENGTH) {
            throw new IllegalArgumentException("Bad hash data : " + hashed);
        }

        String ps = (pass == null) ? "" : pass;

        String salt = hashed.substring(0, SALT_LENGTH);
        return hashed.equals(crypt(ps, salt, DEFAULT_ITERATE_COUNT));
    }

    /**
     * パスワードハッシュを取得する。
     * 
     * @param src
     *            平文パスワード
     * @param salt
     *            salt文字列
     * @param iterateCount
     *            ストレッチングを行う回数
     * @return
     */
    protected static String crypt(String src, String salt, int iterateCount) {

        assert (iterateCount > 0);

        try {
            byte[] saltBin = salt.getBytes("UTF-8");
            byte[] res = src.getBytes("UTF-8");
            for (int i = 0; i < iterateCount; i++) {
                byte[] cont = new byte[saltBin.length + res.length];
                System.arraycopy(saltBin, 0, cont, 0, saltBin.length);
                System.arraycopy(res, 0, cont, saltBin.length, res.length);
                res = DigestUtils.sha256(cont);
            }
            return salt + "$" + (new Base64()).encodeToString(res);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    protected static String newSalt(int length) {
        return RandomStringUtils.random(length, SALT_CHARS);
    }

}
