package de.faustedition.reasoning;

import edu.bath.transitivityutils.ImmutableRelation;
import edu.bath.transitivityutils.Relations;
import edu.bath.transitivityutils.TransitiveRelation;

import java.util.Set;

public class Util {

    public static <E> TransitiveRelation<E> wrapTransitive(ImmutableRelation<E> r, Set<E> universe) {
        TransitiveRelation<E> result = Relations.newTransitiveRelation();
        for (E subject : universe)
            for (E object : universe)
                if (r.areRelated(subject, object))
                    result.relate(subject, object);
        return result;
    }

}
