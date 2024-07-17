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
public class TestCRUDMembershipCard extends JUnitBase{ 

	private EntityManager em;
	private EntityTransaction et;

	private static MembershipCard membershipCard;
	private static Student student;
	private static final byte signed = 1;
	private static final String FIRST_NAME = "Seifeldin";
	private static final String LAST_NAME = "Eid";

	@BeforeAll
	static void setupAllInit() {
		student = new Student();
		student.setFullName(FIRST_NAME, LAST_NAME);

		membershipCard = new MembershipCard();
		membershipCard.setSigned(signed);
		membershipCard.setOwner(student);

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
		// Create query for long as we need the number of found rows
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		// Select count(mc) from MembershipCards mc
		Root<MembershipCard> root = query.from(MembershipCard.class);
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
		em.persist(student);
		em.persist(membershipCard);
		et.commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		// Create query for long as we need the number of found rows
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		// Select count(mc) from membershipCard mc where mc.id = :id
		Root<MembershipCard> root = query.from(MembershipCard.class);
		query.select(builder.count(root));
		query.where(builder.equal(root.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq = em.createQuery(query);
		tq.setParameter("id", membershipCard.getId());
		// Get the result as row count
		long result = tq.getSingleResult();

		// There should only be one row in the DB
		assertThat(result, is(greaterThanOrEqualTo(1L)));
		//		assertEquals(result, 1);
	}


	@Test
	void test03_CreateInvalid() {
		et.begin();
		MembershipCard membershipCard2 = new MembershipCard();
		membershipCard2.setSigned(signed);
//		membershipCard2.setOwner(student);	
		// We expect a failure because Owner is not provided 
		assertThrows(PersistenceException.class, () -> em.persist(membershipCard2));
		et.commit();
	}

	@Test
	void test04_Read() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		// Create query for CourseRegistration
		CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
		// Select cr from CourseRegistration cr
		Root<MembershipCard> root = query.from(MembershipCard.class);
		query.select(root);
		// Create query and set the parameter
		TypedQuery<MembershipCard> tq = em.createQuery(query);
		// Get the result as row count
		List<MembershipCard> membershipCards = tq.getResultList();

		assertThat(membershipCards, contains(equalTo(membershipCard)));
	}

	@Test
	void test05_Update() {
	    CriteriaBuilder builder = em.getCriteriaBuilder();
	    // Create query for Contact
	    CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
	    // Select cr from Contact cr
	    Root<MembershipCard> root = query.from(MembershipCard.class);
	    query.select(root);
	    query.where(builder.equal(root.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
	    // Create query and set the parameter
	    TypedQuery<MembershipCard> tq = em.createQuery(query);
	    tq.setParameter("id", membershipCard.getId());
	    // Get the result as row count
	    MembershipCard returnedMembershipCard = tq.getSingleResult();

	    byte newSigned = 1;
	    Student updateStudent = new Student();
	    updateStudent.setFirstName("Seifeldin");
	    updateStudent.setLastName("Eid");
	    em.persist(updateStudent);

	    et.begin();
	    returnedMembershipCard.setSigned(newSigned);
	    returnedMembershipCard.setOwner(updateStudent);

	    // No need to use em.merge(returnedMembershipCard) since the entity is managed
	    et.commit();

	    // Refresh the entity to get the updated state from the database
	    em.merge(returnedMembershipCard);

	    assertThat(returnedMembershipCard.getSigned(), equalTo(newSigned));
	    assertThat(returnedMembershipCard.getOwner(), equalTo(updateStudent));
	}



	@Test
	void test06_Delete() {
	    CriteriaBuilder builder = em.getCriteriaBuilder();
	    // Create query for Contact
	    CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
	    // Select cr from CourseRegistration cr
	    Root<MembershipCard> root = query.from(MembershipCard.class);
	    query.select(root);
	    query.where(builder.equal(root.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
	    // Create query and set the parameter
	    TypedQuery<MembershipCard> tq = em.createQuery(query);
	    tq.setParameter("id", membershipCard.getId());
	    // Get the result as row count
	    MembershipCard returnedMembershipCard = tq.getSingleResult();

	    et.begin();
	    em.remove(returnedMembershipCard);
	    et.commit();

	    // Create query for long as we need the number of found rows
	    CriteriaQuery<Long> query2 = builder.createQuery(Long.class);
	    // Select count(p) from Professor p where p.id = :id
	    Root<MembershipCard> root2 = query2.from(MembershipCard.class);
	    query2.select(builder.count(root2));
	    query2.where(builder.equal(root2.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
	    // Create query and set the parameter
	    TypedQuery<Long> tq2 = em.createQuery(query2);
	    tq2.setParameter("id", returnedMembershipCard.getId());
	    // Get the result as row count
	    long result = tq2.getSingleResult();
	    assertThat(result, is(equalTo(0L)));
	}
}
