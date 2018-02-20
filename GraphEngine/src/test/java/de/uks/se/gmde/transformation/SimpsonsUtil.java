package de.uks.se.gmde.transformation;

import de.uks.se.gmde.transformation.family.*;

public class SimpsonsUtil
{
	public static Family createFamily()
	{
		Family simpsons = new Family();
		simpsons.withName("Simpson");

		FamilyMember homer = new FamilyMember();
		homer.withGivenName("Homer");

		FamilyMember marge = new FamilyMember();
		marge.withGivenName("Marge");

		FamilyMember bart = new FamilyMember();
		bart.withGivenName("Bart");

		FamilyMember lisa = new FamilyMember();
		lisa.withGivenName("Lisa");

		FamilyMember maggie = new FamilyMember();
		maggie.withGivenName("Maggie");

		simpsons.withFather(homer);
		simpsons.withMother(marge);
		simpsons.withDaughters(lisa, maggie);
		simpsons.withSons(bart);

		return simpsons;
	}

	public static PersonRegister createPersonRegister()
	{
		PersonRegister register = new PersonRegister();
		Person homer = new Man().withFullName("Homer Simpson");
		Person bart = new Man().withFullName("Bart Simpson");

		Person marge = new Woman().withFullName("Marge Simpson");
		Person lisa = new Woman().withFullName("Lisa Simpson");
		Person maggie = new Woman().withFullName("Maggie Simpson");

		return register.withPersons(
			homer,
			bart,
			marge,
			lisa,
			maggie
		);
	}
}
