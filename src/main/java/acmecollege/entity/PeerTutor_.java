package acmecollege.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-07-17T13:37:43.383-0400")
@StaticMetamodel(PeerTutor.class)
public class PeerTutor_ extends PojoBase_ {
	public static volatile SingularAttribute<PeerTutor, String> firstName;
	public static volatile SingularAttribute<PeerTutor, String> lastName;
	public static volatile SingularAttribute<PeerTutor, String> program;
	public static volatile SetAttribute<PeerTutor, PeerTutorRegistration> peerTutorRegistrations;
}
