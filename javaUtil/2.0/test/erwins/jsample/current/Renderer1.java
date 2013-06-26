
package erwins.jsample.current;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ExecutorService에 Callable 을 사용해 Future를 리턴한 후 사용함.
 * HTML의 이미지 다운로드 시간이 많이 걸림으로 병렬화 해서 작업한다. 
 */
public abstract class Renderer1 {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    void renderPage(CharSequence source) {
        final List<ImageInfo> imageInfos = scanForImageInfo(source);
        Callable<List<ImageData>> task = new Callable<List<ImageData>>() {
            public List<ImageData> call() {
                List<ImageData> result = new ArrayList<ImageData>();
                for (ImageInfo imageInfo : imageInfos)
                    result.add(imageInfo.downloadImage());
                return result;
            }
        };

        Future<List<ImageData>> future = executor.submit(task);
        renderText(source); //텍스트를 먼저 렌더링한 후

        try{
            List<ImageData> imageData = future.get(); //나중에 이미지 작업 task.get(timeout, unit); 처럼 파라메터 가능
            for (ImageData data : imageData)
                renderImage(data);
        }
        catch (InterruptedException e) {
            // Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
            // We don't need the result, so cancel the task too
            future.cancel(true);
        }
        catch (ExecutionException e) {
        	throw new RuntimeException(e);
        }
    }

    interface ImageData {

    }

    interface ImageInfo {
        ImageData downloadImage();
    }

    abstract void renderText(CharSequence s);

    abstract List<ImageInfo> scanForImageInfo(CharSequence s);

    abstract void renderImage(ImageData i);
}
