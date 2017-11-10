package vitdube.com.vidtube;

/**
 * Created by xingjia.zhang on 4/11/17.
 */

public abstract class PostTaskListener<K> {
    abstract void onPostTask(K result);
}
