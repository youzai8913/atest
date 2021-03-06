package atest.access_gate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
public class App implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		final int taskCount = Runtime.getRuntime().availableProcessors();
		final Task task = new Task(taskCount);
		final ExecutorService executor = Executors.newCachedThreadPool();
		final StopWatch stopWatch = new StopWatch();
		logger.info("taskCount: {}", taskCount);
		
		stopWatch.start();
		for (int i = 0; i < taskCount; ++i) {
			executor.submit(task);
		}
		try {
			task.awaitReady();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		logger.info("task ready: {}", stopWatch);
		
		task.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// pass
		}
		task.stop();
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		logger.info("task done: {}", stopWatch);
		
		do {
			long delay = task.getDelay(System.currentTimeMillis());
			int count = task.gate.getAndResetCount();
			task.countSum.addAndGet(count);
			logger.info("delay={} count={} hello", delay, count);
		} while (false);
		
		long count = task.count.get();
		long countSum = task.countSum.get();
		logger.info("count={} countSum={} equals={}", count, countSum, count == countSum);
	}
	
}
