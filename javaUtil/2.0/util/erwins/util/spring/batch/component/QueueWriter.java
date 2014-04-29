package erwins.util.spring.batch.component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.google.common.collect.Lists;

import erwins.util.lib.ExceptionUtil;

/**
 *  Queue에는 병렬로 데이터가 입력된다. 
 *  1. Queue에서 commitInterval만큼의 데이터를 꺼냈거나
 *  2. Queue에 데이터가 없어서 timeout만큼 대기했다면
 *  itemWriter를 작동시킨다. 
 *  많은 요청을 단일 스래드로 배치 처리할때 주로 사용된다. (불특정 다수 로그의 DB입력 등)
 *  */
@Data
public class QueueWriter<T> implements Runnable{
	
	private final BlockingQueue<T> queue;
	private final ItemWriter<T> itemWriter;
	private TimeUnit unit = TimeUnit.SECONDS;
	private int timeout = 2;
	private int commitInterval = 1000;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void run() {
		Thread current = Thread.currentThread();
		List<T> items = Lists.newArrayList();
		log.info(current.getName() + " thread start");
		try {
			while(!current.isInterrupted()){
				T item = queue.poll(timeout, unit);
				if(item==null){
					//타임아웃이 된 경우
					doItemWrite(items);
				}else{
					items.add(item);
					if(items.size() >= commitInterval ){
						doItemWrite(items);
					}
				}
			}
		} catch (InterruptedException e) {
			//마지막 남은 큐를 처리하고 죽는다.
			log.info(this.getClass().getSimpleName() + " Interrupted! remain queue size : " + queue.size());
			doItemWrite(items);
			current.interrupt(); //혹시나. while밖에 있어서 안해도 끝나긴 한다.
		}
		log.info(current.getName() + " thread end");
	}

	protected void doItemWrite(List<T> items){
		try {
			if(items.size() > 0 ){
				itemWriter.write(items);
				items.clear();
			}
		} catch (Exception e) {
			ExceptionUtil.throwException(e);
		}
	}

}
