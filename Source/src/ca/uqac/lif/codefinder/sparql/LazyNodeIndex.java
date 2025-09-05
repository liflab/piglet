package ca.uqac.lif.codefinder.sparql;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LazyNodeIndex<T,U>
{
	Map<String, T> nodesByIri = new ConcurrentHashMap<>();
	Map<String, U> typeCache = new ConcurrentHashMap<>(); // iri -> type IRI
	
	public void put(String iri, T node)
	{
		nodesByIri.put(iri, node);
	}
	
	public T get(String iri)
	{
		return nodesByIri.get(iri);
	}
	
	public Set<String> allIris()
	{
		return nodesByIri.keySet();
	}
}
