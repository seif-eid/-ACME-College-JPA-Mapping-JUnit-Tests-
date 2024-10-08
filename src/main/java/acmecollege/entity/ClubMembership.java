/********************************************************************************************************2*4*w*
 * File:  ClubMembership.java Course materials CST 8277
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmecollege.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Column;

@SuppressWarnings("unused")

/**
 * The persistent class for the club_membership database table.
 */
//TODO CM01 - Add the missing annotations.
//TODO CM02 - Do we need a mapped super class?  If so, which one?
@Entity(name="clubMembership")
@Table(name="club_membership")
@AttributeOverride(name="id", column=@Column(name="membership_id"))
public class ClubMembership extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

	// TODO CM03 - Add annotations for M:1.  Changes to this class should cascade to StudentClub.
	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "club_id", referencedColumnName = "club_id")
	private StudentClub club;

	// TODO CM04 - Add annotations for 1:1.  Changes to this class should not cascade to MembershipCard.
	@OneToOne(mappedBy = "clubMembership")
	private MembershipCard card;

	@Embedded
	private DurationAndStatus durationAndStatus;

	public ClubMembership() {
		durationAndStatus = new DurationAndStatus();
	}

	public StudentClub getStudentClub() {
		return club;
	}

	public void setStudentClub(StudentClub club) {
		this.club = club;
		//We must manually set the 'other' side of the relationship (JPA does not 'do' auto-management of relationships)
		if (club != null) {
			club.getClubMemberships().add(this);
		}
	}

	public MembershipCard getCard() {
		return card;
	}

	public void setCard(MembershipCard card) {
		this.card = card;
		//We should (as above) set the 'other' side of this relationship, but that causes an infinite-loop
	}

	public DurationAndStatus getDurationAndStatus() {
		return durationAndStatus;
	}

	public void setDurationAndStatus(DurationAndStatus durationAndStatus) {
		this.durationAndStatus = durationAndStatus;
	}
	
	//Inherited hashCode/equals NOT sufficient for this Entity class
	/**
	 * Very important:  Use getter's for member variables because JPA sometimes needs to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		
		// include DurationAndStatus in identity
		return prime * result + Objects.hash(getId(), getDurationAndStatus());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof ClubMembership otherClubMembership) {
			// See comment (above) in hashCode():  Compare using only member variables that are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherClubMembership.getId()) &&
				Objects.equals(this.getDurationAndStatus(), otherClubMembership.getDurationAndStatus());
		}
		return false;
	}
}
