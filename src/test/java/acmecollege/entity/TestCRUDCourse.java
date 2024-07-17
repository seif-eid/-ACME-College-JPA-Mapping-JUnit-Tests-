package acmecollege.entity;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import common.JUnitBase;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestCRUDCourse extends JUnitBase {
	private EntityManager em;
	private EntityTransaction et;
	
	private static Course course;
	private static String course_code = "CST8277";
	private static String course_title = "Enterprise Application";
	private static int year = 2023;
	private static String semester = "FALL";
	private static int credit_units = 3;
	private static byte online = 0;
	
	@BeforeAll
	static void setupAllInit() {
	course = new Course();
	course.setCourse(course_code,course_title, year, semester, credit_units, online );
	
	}

	@BeforeEach
	void setup() {
		em = getEntityManager();
		et = em.getTransaction();
	}

	

	@AfterEach
	void tearDown() {
		em.close();
	}
	
	@Test
	void test01_Empty() {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		// Create query for long 
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		// Select count(*) from CourseRegistration cr
		Root<Course> root = query.from(Course.class);
		query.select(builder.count(root));
		// Create query and set the parameter
		TypedQuery<Long> tq = em.createQuery(query);
		// Get the result as row count
		long result = tq.getSingleResult();
		assertThat(result, is(comparesEqualTo(0L)));

	}
	
	@Test
	void test02_Create() {
		et.begin();
		em.persist(course);
		et.commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		// Create query for long as we need the number of found rows
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		// Select count(cr) from Course cr where cr.id = :id
		Root<Course> root = query.from(Course.class);
		query.select(builder.count(root));
		query.where(builder.equal(root.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq = em.createQuery(query);
		tq.setParameter("id", course.getId());
		// Get the result as row count
		long result = tq.getSingleResult();

		// Must be only be one row
		assertThat(result, is(greaterThanOrEqualTo(1L)));
//		assertEquals(result, 1);
	}
	
	@Test
	void test03_CreateInvalid() {
		et.begin();
		Course course2 = new Course();
		course2.setCourseCode(course_code);
//		course2.setCourseTitle(course_title);
		course2.setYear(year);
		course2.setSemester(semester);
		course2.setCreditUnits(credit_units);
		course2.setOnline(online);
	
		// We expect a failure because course_title is a not null field in the database
		assertThrows(PersistenceException.class, () -> em.persist(course2));
		et.commit();
	}

	
	
	
	@Test
	void test04_Read() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		// Create query for Course
		CriteriaQuery<Course> query = builder.createQuery(Course.class);
		// Select cr from Course cr
		Root<Course> root = query.from(Course.class);
		query.select(root);
		// Create query and set the parameter
		TypedQuery<Course> tq = em.createQuery(query);
		// Get the result as row count
		List<Course> Courses = tq.getResultList();

		assertThat(Courses, contains(equalTo(course)));
	}
	
	@Test
	void test05_Update() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Course> query = builder.createQuery(Course.class);
		Root<Course> root = query.from(Course.class);
		query.select(root);
		query.where(builder.equal(root.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Course> tq = em.createQuery(query);
		tq.setParameter("id", course.getId());
		// Get the result as row count
		Course returnedCourse = tq.getSingleResult();

		String newCourseCode = "CST2222";
		String newCourseTitle = "Network";
		int newYear = 2023;
		String newSemester = "Fall";
		int newCredit_units = 2;
		byte newOnline = 1;

		et.begin();
		
		returnedCourse.setCourseCode(newCourseCode);
		returnedCourse.setCourseTitle(newCourseTitle);
		returnedCourse.setYear(newYear);
		returnedCourse.setSemester(newSemester);
		returnedCourse.setCreditUnits(newCredit_units);
		returnedCourse.setOnline(newOnline);
		
		em.merge(returnedCourse);
		et.commit();

		returnedCourse = tq.getSingleResult();

		assertThat(returnedCourse.getCourseCode(), equalTo(newCourseCode));
		assertThat(returnedCourse.getCourseTitle(), equalTo(newCourseTitle));
		assertThat(returnedCourse.getYear(), equalTo(newYear));
		assertThat(returnedCourse.getSemester(), equalTo(newSemester));
		assertThat(returnedCourse.getCreditUnits(), equalTo(newCredit_units));
		assertThat(returnedCourse.getOnline(), equalTo(newOnline));
	}
	
	@Test
	void test06_Delete() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		// Create query for Course
		CriteriaQuery<Course> query = builder.createQuery(Course.class);
		// Select cr from Course cr
		Root<Course> root = query.from(Course.class);
		query.select(root);
		query.where(builder.equal(root.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Course> tq = em.createQuery(query);
		tq.setParameter("id", course.getId());
		// Get the result as row count
		Course returnedCourse = tq.getSingleResult();

		et.begin();
		// Add another row to db to make sure only the correct row is deleted
		Course course2 = new Course();
		course2.setCourseCode("CST8888");
		course2.setCourseTitle("Advanced Database");
		course2.setCreditUnits(3);
		course2.setOnline(online);
		course2.setSemester("Fall");
		course2.setYear(2023);
		em.persist(course2);
		et.commit();

		et.begin();
		em.remove(returnedCourse);
		et.commit();

		// Create query for long as we need the number of found rows
		CriteriaQuery<Long> query2 = builder.createQuery(Long.class);
		// Select count(p) from Professor p where p.id = :id
		Root<Course> root2 = query2.from(Course.class);
		query2.select(builder.count(root2));
		query2.where(builder.equal(root2.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq2 = em.createQuery(query2);
		tq2.setParameter("id", returnedCourse.getId());
		// Get the result as row count
		long result = tq2.getSingleResult();
		assertThat(result, is(equalTo(0L)));

		// Create query and set the parameter
		TypedQuery<Long> tq3 = em.createQuery(query2);
		tq3.setParameter("id", course2.getId());
		// Get the result as row count
		result = tq3.getSingleResult();
		assertThat(result, is(equalTo(1L)));
	}
}
