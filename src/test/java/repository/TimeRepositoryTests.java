package repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.checkersplusplus.dao.TimeRepository;

import config.TestJpaConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  classes = { TestJpaConfig.class }, 
  loader = AnnotationConfigContextLoader.class)
public class TimeRepositoryTests {

	@Autowired
	private TimeRepository timeRepository;
	
	@Test
	public void assertGetCurrentTimestamp() {
		Date date = timeRepository.getCurrentTimestamp();
		DateTime now = new DateTime();
		DateTime nowFromDb = new DateTime(date);
		assertNotNull(date);
		assertEquals(now.getYear(), nowFromDb.getYear());
		assertEquals(now.getMonthOfYear(), nowFromDb.getMonthOfYear());
		assertEquals(now.getDayOfMonth(), nowFromDb.getDayOfMonth());
		assertEquals(now.getHourOfDay(), nowFromDb.getHourOfDay());
		assertEquals(now.getMinuteOfHour(), nowFromDb.getMinuteOfHour());	
	}
}
