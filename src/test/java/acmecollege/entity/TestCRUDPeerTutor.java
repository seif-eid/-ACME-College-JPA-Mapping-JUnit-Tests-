package acmecollege.entity;

import static org.hamcrest.MatcherAssert.assertThat;
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
public class TestCRUDPeerTutor extends JUnitBase {
    private EntityManager em;
    private EntityTransaction et;
    
    private static PeerTutor peerTutor;
    private static String firstName = "Seifeldin";
    private static String lastName = "Eid";
    private static String program = "Computer Science";
    
    @BeforeAll
    static void setupAllInit() {
    	peerTutor = new PeerTutor(); 	
    	peerTutor.setPeerTutor(firstName, lastName, program);// Initialize with null registrations
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
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<PeerTutor> root = query.from(PeerTutor.class);
        query.select(builder.count(root));
        TypedQuery<Long> tq = em.createQuery(query);
        long result = tq.getSingleResult();
        assertThat(result, is(equalTo(0L)));
    }
    @Test
    void test02_Create() {
        et.begin();
        em.persist(peerTutor);
        et.commit();
        
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<PeerTutor> root = query.from(PeerTutor.class);
        query.select(builder.count(root));
        query.where(builder.equal(root.get(PojoBase_.id), builder.parameter(Integer.class, "id")));
        TypedQuery<Long> tq = em.createQuery(query);
        tq.setParameter("id", peerTutor.getId());
        long result = tq.getSingleResult();
        
        assertThat(result, is(greaterThanOrEqualTo(1L)));
    }
    @Test
    void test03_CreateInvalid() {
        et.begin();
        PeerTutor peerTutor2 = new PeerTutor();
        peerTutor2.setFirstName(null);
        peerTutor2.setLastName(lastName);
        peerTutor2.setProgram(program);
        
		// We expect a failure because course_title is a not null field in the database
        assertThrows(PersistenceException.class, () -> em.persist(peerTutor2));
        et.commit();
    }
    
    @Test
    void test04_Read() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PeerTutor> query = builder.createQuery(PeerTutor.class);
        Root<PeerTutor> root = query.from(PeerTutor.class);
        query.select(root);
        TypedQuery<PeerTutor> tq = em.createQuery(query);
        List<PeerTutor> tutors = tq.getResultList();
        
        assertThat(tutors, contains(peerTutor));
    }
    
    @Test
    void test05_Update() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PeerTutor> query = builder.createQuery(PeerTutor.class);
        Root<PeerTutor> root = query.from(PeerTutor.class);
        query.select(root);
        query.where(builder.equal(root.get(PojoBase_.id), builder.parameter(Integer.class, "id")));
        TypedQuery<PeerTutor> tq = em.createQuery(query);
        tq.setParameter("id", peerTutor.getId());
        PeerTutor returnedTutor = tq.getSingleResult();
        
        String newFirstName = "Jane";
        String newLastName = "Smith";
        String newProgram = "Software Engineering";
        
        et.begin();
        returnedTutor.setFirstName(newFirstName);
        returnedTutor.setLastName(newLastName);
        returnedTutor.setProgram(newProgram);
        em.merge(returnedTutor);
        et.commit();
        
        returnedTutor = tq.getSingleResult();
        
        assertThat(returnedTutor.getFirstName(), equalTo(newFirstName));
        assertThat(returnedTutor.getLastName(), equalTo(newLastName));
        assertThat(returnedTutor.getProgram(), equalTo(newProgram));
    }
    
    @Test
    void test06_Delete() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PeerTutor> query = builder.createQuery(PeerTutor.class);
        Root<PeerTutor> root = query.from(PeerTutor.class);
        query.select(root);
        query.where(builder.equal(root.get(PojoBase_.id), builder.parameter(Integer.class, "id")));
        TypedQuery<PeerTutor> tq = em.createQuery(query);
        tq.setParameter("id", peerTutor.getId());
        PeerTutor returnedTutor = tq.getSingleResult();
        
        et.begin();
		// Add another row to db to make sure only the correct row is deleted
        PeerTutor newPeerTutor = new PeerTutor("Sam", "Adams", "Mathematics", null); 
        em.persist(newPeerTutor);
        em.remove(returnedTutor);
        et.commit();
        
        TypedQuery<Long> tq2 = em.createQuery("SELECT COUNT(pt) FROM PeerTutor pt WHERE pt.id = :id", Long.class);
        tq2.setParameter("id", returnedTutor.getId());
        long result = tq2.getSingleResult();
        assertThat(result, equalTo(0L));
        
        TypedQuery<Long> tq3 = em.createQuery("SELECT COUNT(pt) FROM PeerTutor pt WHERE pt.id = :id", Long.class);
        tq3.setParameter("id", newPeerTutor.getId());
        result = tq3.getSingleResult();
        assertThat(result, equalTo(1L));
    }
}