import java.lang.reflect.Method;

/**
 * Java7の"try-with-resources"を、Java7対応以前のリソース関連クラスに適用するラッパークラス。<br>
 * "try-with-resources"にリソース管理をさせるためには、該当のリソースオブジェクトは
 * {@link java.lang.AutoCloseable} を 実装している必要がある。 ACWrapperは
 * {@link java.lang.AutoCloseable}を実装しており、インターフェース問わず void close() メソッドを持つ
 * 全てのクラスに適用可能なProxy classとして動作する。<br>
 * 例えば、OldResourceというリソースオブジェクトがあったとして、以下のように実装することで、
 * tryブロック終了後にOldResource#close() が自動的に呼ばれる。
 * 
 * {@code
 *     try (ACWrapper<OldResource> acw = new ACWrapper<>(new OldResource(param))) {
 *         OldResource resource = ACWrapper.getResource();
 *     
 *         // OldResourceを利用した処理…
 *     }
 * }
 */
public class ACWrapper<T> implements AutoCloseable {

    private final T resource;
    private final Method closeMethod;

    public ACWrapper(T resource) {

        try {
            closeMethod = resource.getClass().getMethod("close");
        } catch (Exception e) {
            throw new IllegalArgumentException("close-method not found.", e);
        }
        this.resource = resource;
    }

    @Override
    public void close() throws Exception {
        closeMethod.invoke(resource);
    }

    public T getResource() {
        return resource;
    }

}