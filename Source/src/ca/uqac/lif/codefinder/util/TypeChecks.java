/*
    Analysis of assertions in Java programs
    Copyright (C) 2025 Sylvain Hallé, Sarika Machhindra Kadam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.codefinder.util;

import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.resolution.types.ResolvedWildcard;

import java.util.Objects;

/**
 * Utility methods to check types.
 */
public final class TypeChecks
{
	private TypeChecks()
	{
		super();
	}

	/**
	 * True if `t` is (or extends/implements) the target FQN (e.g.,
	 * "java.util.Map").
	 */
	public static boolean isSubtypeOf(ResolvedType t, String targetFqn)
	{
		Objects.requireNonNull(t, "type");
		Objects.requireNonNull(targetFqn, "targetFqn");

		// 1) Concrete class/interface types (with generics)
		if (t.isReferenceType())
		{
			ResolvedReferenceType ref = t.asReferenceType();
			if (targetFqn.equals(ref.getQualifiedName()))
				return true;
			// check all ancestors (superclass + interfaces)
			for (ResolvedReferenceType a : ref.getAllAncestors())
			{
				if (targetFqn.equals(a.getQualifiedName()))
					return true;
			}
			return false;
		}

		// 2) Type variables: check "extends" bounds, e.g., <T extends Map<?,?>>
		if (t.isTypeVariable())
		{
			ResolvedTypeVariable tv = t.asTypeVariable();
			ResolvedTypeParameterDeclaration tp = tv.asTypeParameter();
			for (ResolvedTypeParameterDeclaration.Bound b : tp.getBounds())
			{
				if (b.isExtends() && isSubtypeOf(b.getType(), targetFqn))
					return true;
			}
			return false;
		}

		// 3) Wildcards: ? extends X / ? super X / unbounded
		if (t.isWildcard())
		{
			ResolvedWildcard w = t.asWildcard();
			// Older JP: getBoundedType() is a direct value when bounded; use
			// isExtends()/isSuper()/isBounded()
			if (w.isExtends() && w.isBounded())
			{
				ResolvedType bt = w.getBoundedType();
				return isSubtypeOf(bt, targetFqn);
			}
			// '? super X' or unbounded doesn't make the wildcard a subtype of target
			return false;
		}

		// 4) Arrays, primitives, null, unions/intersections (not handled here)
		return false;
	}
}
